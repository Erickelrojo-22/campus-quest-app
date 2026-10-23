package com.example.gamequest.data.session

import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.preferences.UserPreferences
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SessionCoordinatorTest {
    private class Prefs : UserPreferencesRepository() {
        val data = MutableStateFlow(UserPreferences(usuarioActivoId = 99))
        var delayRead = 0L
        var delayWrite = 0L
        val writes = mutableListOf<Int>()
        override val preferencias = flow { delay(delayRead); emitAll(data) }
        override suspend fun setUsuarioActivoId(id: Int) { delay(delayWrite); writes += id; data.value = data.value.copy(usuarioActivoId = id) }
        override suspend fun cerrarSesion() = setUsuarioActivoId(-1)
    }
    private class Access : SessionAccess {
        var resolve: suspend () -> UsuarioEntity = { error("Not expected") }
        val sync = mutableListOf<Int>()
        override suspend fun resolverIdentidad() = resolve()
        override suspend fun sincronizarCatalogoYProgreso(localUserId: Int) { sync += localUserId }
    }
    private fun user(id: Int = 8, remoteId: Int? = 51) = UsuarioEntity(id, "Account", "", "", "", remoteId = remoteId)

    @Test fun sinToken_esperaLimpiezaAntesDeSignedOut() = runTest {
        val prefs = Prefs().apply { delayWrite = 2000 }
        val sut = SessionCoordinator(FakeTokenStore(), prefs, Access(), { null }, {}, backgroundScope)
        launch { sut.restore() }; runCurrent()
        assertEquals(SessionState.Restoring, sut.state.value)
        advanceTimeBy(2001); runCurrent()
        assertEquals(SessionState.SignedOut(), sut.state.value)
        assertEquals(listOf(-1), prefs.writes)
    }
    @Test fun preferenciasLentas_noAutenticaPorIdObsoleto() = runTest {
        val prefs = Prefs().apply { delayRead = 3000 }
        val access = Access().apply { resolve = { user() } }
        val sut = SessionCoordinator(FakeTokenStore("token").apply { save("token", 8) }, prefs, access, { user() }, {}, backgroundScope)
        launch { sut.restore() }; runCurrent(); advanceTimeBy(1500); runCurrent()
        assertEquals(SessionState.Restoring, sut.state.value)
        assertTrue(access.sync.isEmpty())
        advanceUntilIdle()
        assertEquals(SessionState.Authenticated(8, SessionSource.REMOTE, false), sut.state.value)
        assertEquals(listOf(8), prefs.writes)
    }
    @Test fun tokenValido_resuelveAntesDelCatalogo() = runTest {
        val order = mutableListOf<String>()
        val prefs = Prefs()
        val access = object : SessionAccess {
            override suspend fun resolverIdentidad(): UsuarioEntity { order += "identity"; return user() }
            override suspend fun sincronizarCatalogoYProgreso(localUserId: Int) { order += "catalog"; delay(10000) }
        }
        val sut = SessionCoordinator(FakeTokenStore("legacy"), prefs, access, { user() }, {}, backgroundScope)
        sut.restore(); assertEquals(SessionState.Authenticated(8,SessionSource.REMOTE,false),sut.state.value)
        runCurrent(); assertEquals(listOf("identity","catalog"),order)
    }
    @Test fun tokenAntiguo_sinVinculo_sinRed_requiereRecuperacion() = runTest {
        val access = Access().apply { resolve = { throw IOException("offline") } }
        val sut = SessionCoordinator(FakeTokenStore("legacy"),Prefs(),access,{ user(99) },{},backgroundScope)
        sut.restore(); assertTrue(sut.state.value is SessionState.RecoveryRequired); assertTrue(access.sync.isEmpty())
    }
    @Test fun visitanteAntiguo_conservaIdLocal() = runTest {
        val tokens = FakeTokenStore("offline-visitor-51")
        val access = Access().apply { resolve = { user(51,null) } }
        val sut = SessionCoordinator(tokens,Prefs(),access,{ user(51,null) },{},backgroundScope)
        sut.restore(); assertEquals(SessionState.Authenticated(51,SessionSource.LOCAL_VISITOR,true),sut.state.value)
        assertEquals(51,tokens.snapshot.localUserId)
    }
    @Test fun sinRed_conVinculoValido_usaCache() = runTest {
        val tokens = FakeTokenStore().apply { save("token",8) }
        val access = Access().apply { resolve = { throw IOException("offline") } }
        val sut = SessionCoordinator(tokens,Prefs(),access,{ user() },{},backgroundScope)
        sut.restore(); assertEquals(SessionState.Authenticated(8,SessionSource.REMOTE,true),sut.state.value)
    }
    @Test fun sinRed_sinCache_requiereRecuperacion() = runTest {
        val tokens = FakeTokenStore().apply { save("token",8) }
        val access = Access().apply { resolve = { throw IOException("offline") } }
        val sut = SessionCoordinator(tokens,Prefs(),access,{ null },{},backgroundScope)
        sut.restore(); assertTrue(sut.state.value is SessionState.RecoveryRequired)
    }
    @Test fun http401_invalidaSesion() = runTest {
        val tokens = FakeTokenStore().apply { save("token",8) }
        val prefs = Prefs()
        val access = Access().apply { resolve = { throw ApiException(401,"expired") } }
        val sut = SessionCoordinator(tokens,prefs,access,{ user() },{},backgroundScope)
        sut.restore(); assertTrue(sut.state.value is SessionState.SignedOut); assertNull(tokens.token); assertEquals(listOf(-1),prefs.writes)
    }
    @Test fun resultadoTardio_noCambiaLoginNuevo() = runTest {
        val tokens = FakeTokenStore().apply { save("old",8) }
        val pending = CompletableDeferred<UsuarioEntity>()
        val access = Access().apply { resolve = { pending.await() } }
        val prefs = Prefs()
        val sut = SessionCoordinator(tokens,prefs,access,{ id -> user(id,if(id==8)51 else 77) },{},backgroundScope)
        launch { sut.restore() }; runCurrent()
        sut.commitAuthenticated("new",9)
        pending.complete(user()); runCurrent()
        sut.unauthorized("old")
        assertEquals("new",tokens.token); assertEquals(SessionState.Authenticated(9,SessionSource.REMOTE,false),sut.state.value)
        assertEquals(listOf(9),prefs.writes)
    }
    @Test fun cancelacion_noPublicaErrorDeRed() = runTest {
        val access = Access().apply { resolve = { throw CancellationException("cancel") } }
        val sut = SessionCoordinator(FakeTokenStore("legacy"),Prefs(),access,{ null },{},backgroundScope)
        try { sut.restore(); fail("Cancellation must propagate") } catch (_: CancellationException) { }
        assertEquals(SessionState.Restoring,sut.state.value)
    }
    @Test fun logout_limpiaLocalAntesDeRevocarConTokenCapturado() = runTest {
        val tokens = FakeTokenStore().apply { save("old",8) }
        val prefs = Prefs()
        lateinit var sut: SessionCoordinator
        var revoked: String? = null
        sut = SessionCoordinator(tokens,prefs,Access(),{ id -> user(id) },{ token ->
            assertNull(tokens.token); assertTrue(sut.state.value is SessionState.SignedOut); assertEquals(-1,prefs.data.value.usuarioActivoId)
            revoked = token; delay(10000)
        },backgroundScope)
        launch { sut.logout() }; runCurrent()
        assertEquals("old",revoked)
        sut.commitAuthenticated("new",9)
        advanceUntilIdle()
        assertEquals("new",tokens.token); assertEquals(9,(sut.state.value as SessionState.Authenticated).localUserId)
    }
    @Test fun acceptLogin_esIdempotente() = runTest {
        val prefs = Prefs(); val tokens = FakeTokenStore()
        val sut = SessionCoordinator(tokens,prefs,Access(),{ user() },{},backgroundScope)
        sut.commitAuthenticated("token",8); sut.acceptLogin(8)
        assertEquals(listOf(8),prefs.writes); assertEquals(listOf("token"),tokens.saved)
    }
    @Test fun commitCancelado_noPublicaErrorNiConservaToken() = runTest {
        val prefs = Prefs().apply { delayWrite = 2000 }
        val tokens = FakeTokenStore()
        val sut = SessionCoordinator(tokens,prefs,Access(),{ user() },{},backgroundScope)
        val login = launch { sut.commitAuthenticated("new",8) }
        runCurrent()
        login.cancel(); runCurrent()
        assertNull(tokens.token)
        assertEquals(SessionState.SignedOut(),sut.state.value)
    }
}
