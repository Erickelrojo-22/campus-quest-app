package com.example.gamequest.data.repository

import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.remote.*
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.json.JSONException
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryRemoteTest {
    private val dao = FakeUsuarioDao()
    private val tokens = FakeTokenStore("previous-session")
    private val gateway = FakeCampusGateway(tokens)
    private val repository = AuthRepository(dao, gateway)
    private val remote = RemoteUserDto(51, "Remote", "student@live.uleam.edu.ec", "Software", 0, 1, "estudiante")

    @Test fun visitante_rechazoHttp_noCreaSesionLocal() = runTest {
        for (status in listOf(400, 401, 403, 422, 429, 500)) {
            gateway.authentication = { _, _ -> throw ApiException(status, "rejected $status") }
            assertTrue("HTTP $status must be returned", repository.entrarConNombre("Visitor", false) is AuthResult.Error)
            assertTrue(dao.users.isEmpty())
            assertEquals("previous-session", tokens.token)
            assertTrue(tokens.saved.isEmpty())
        }
    }
    @Test fun visitante_errorTransporte_permitaFallback() = runTest {
        gateway.authentication = { _, _ -> throw IOException("offline") }
        val result = repository.entrarConNombre("Visitor", false) as AuthResult.Exito
        assertEquals("offline-visitor-${result.usuario.id}", tokens.token)
        assertEquals(1, dao.users.size)
        assertTrue(result.usuario.correoInstitucional.isEmpty())
    }
    @Test fun visitante_cancelacion_noCreaSesionLocal() = runTest {
        gateway.authentication = { _, _ -> throw CancellationException("cancelled") }
        try { repository.entrarConNombre("Visitor", false); fail("Cancellation must propagate") }
        catch (_: CancellationException) { }
        assertTrue(dao.users.isEmpty()); assertTrue(tokens.saved.isEmpty())
    }
    @Test fun visitante_respuestaInvalida_noCreaSesionLocal() = runTest {
        gateway.authentication = { _, _ -> throw JSONException("invalid JSON") }
        assertTrue(repository.entrarConNombre("Visitor", false) is AuthResult.Error)
        assertTrue(dao.users.isEmpty()); assertEquals("previous-session", tokens.token)
    }
    @Test fun visitante_exitoRemoto_noCreaVisitante() = runTest {
        gateway.authentication = { _, _ -> AuthPayload("real-server-token", remote) }
        val result = repository.entrarConNombre("Visitor", false) as AuthResult.Exito
        assertEquals(remote.remoteId, result.usuario.remoteId)
        assertEquals(result.usuario.id, tokens.snapshot.localUserId)
        assertEquals(listOf("real-server-token"), tokens.saved)
        assertEquals(listOf(result.usuario), dao.users)
    }
    @Test fun persistenciaRoomFallida_noPermiteFallback() = runTest {
        gateway.authentication = { _, _ -> AuthPayload("real-server-token", remote) }
        dao.failSave = IOException("disk error")
        assertTrue(repository.entrarConNombre("Visitor", false) is AuthResult.Error)
        assertTrue(dao.users.isEmpty()); assertEquals("previous-session", tokens.token)
    }
    @Test fun persistenciaTokenFallida_noPermiteFallback() = runTest {
        gateway.authentication = { _, _ -> AuthPayload("real-server-token", remote) }
        tokens.failRemoteSave = true
        assertTrue(repository.entrarConNombre("Visitor", false) is AuthResult.Error)
        assertFalse(dao.users.any { it.correoInstitucional.isEmpty() })
        assertEquals("previous-session", tokens.token)
    }
}
