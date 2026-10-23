package com.example.gamequest.data.session

import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The only writer of token bindings, active-user preference and session state. */
class SessionCoordinator(
    private val tokens: TokenStore,
    private val preferences: UserPreferencesRepository,
    private val access: SessionAccess,
    private val cachedUser: suspend (Int) -> UsuarioEntity?,
    private val revoke: suspend (String) -> Unit,
    private val scope: CoroutineScope
) : SessionCommitter {
    private val lock = Mutex()
    private var generation = 0L
    private var syncJob: Job? = null
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Restoring)
    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    override suspend fun commitAuthenticated(token: String, localUserId: Int) {
        lock.withLock {
            val user = requireNotNull(cachedUser(localUserId)) { "No existe la cuenta local." }
            require(user.id == localUserId) { "Cuenta local inconsistente." }
            val local = token.startsWith("offline-")
            require(if (local) validVisitor(user, token) else user.remoteId != null) { "Identidad de sesión inválida." }
            generation++
            syncJob?.cancel()
            // A failed preference write leaves no usable binding for the new session.
            // The token is the authority; preferences alone never grant access.
            tokens.save(token, localUserId)
            try { preferences.setUsuarioActivoId(localUserId) }
            catch (e: CancellationException) {
                tokens.clearIfCurrent(token)
                mutableState.value = SessionState.SignedOut()
                throw e
            }
            catch (e: Exception) {
                tokens.clearIfCurrent(token)
                mutableState.value = SessionState.RecoveryRequired("No se pudo guardar la sesión.")
                throw e
            }
            mutableState.value = SessionState.Authenticated(localUserId, if(local) SessionSource.LOCAL_VISITOR else SessionSource.REMOTE, local)
            if (!local) startSync(generation, tokens.snapshot, localUserId)
        }
    }

    suspend fun acceptLogin(localUserId: Int) = lock.withLock {
        val current = mutableState.value as? SessionState.Authenticated
        if (tokens.snapshot.localUserId != localUserId || tokens.token == null) return@withLock
        if (current?.localUserId == localUserId) return@withLock
        val token = requireNotNull(tokens.token)
        val user = requireNotNull(cachedUser(localUserId))
        require(if(token.startsWith("offline-")) validVisitor(user,token) else user.remoteId != null)
        generation++
        preferences.setUsuarioActivoId(localUserId)
        mutableState.value = SessionState.Authenticated(localUserId, if(token.startsWith("offline-")) SessionSource.LOCAL_VISITOR else SessionSource.REMOTE, token.startsWith("offline-"))
    }

    suspend fun restore() {
        val attempt = lock.withLock {
            generation++
            syncJob?.cancel()
            mutableState.value = SessionState.Restoring
            try { preferences.preferencias.first() }
            catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                mutableState.value = SessionState.RecoveryRequired("No se pudieron leer las preferencias. Reintenta.")
                return
            }
            val snapshot = tokens.snapshot
            if (snapshot.token == null) {
                try { preferences.cerrarSesion(); mutableState.value = SessionState.SignedOut() }
                catch (e: CancellationException) { throw e
                } catch (_: Exception) { mutableState.value = SessionState.RecoveryRequired("No se pudo limpiar la sesión. Reintenta.") }
                return
            }
            generation to snapshot
        }
        val (version, snapshot) = attempt
        val user = try { access.resolverIdentidad() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            lock.withLock {
                if (version != generation || tokens.snapshot != snapshot) return
                if (e is ApiException && e.status == 401) invalidateLocked(requireNotNull(snapshot.token))
                else {
                    val cached = if (e.permiteFallbackOffline()) snapshot.localUserId?.let { cachedUser(it) } else null
                    val valid = cached != null && cached.id == snapshot.localUserId && cached.remoteId != null && !snapshot.token.orEmpty().startsWith("offline-")
                    try {
                        if (valid) {
                            preferences.setUsuarioActivoId(cached!!.id)
                            mutableState.value = SessionState.Authenticated(cached.id,SessionSource.REMOTE,true)
                        } else {
                            preferences.cerrarSesion()
                            mutableState.value = SessionState.RecoveryRequired(e.message ?: "No se pudo restaurar la sesi\u00f3n.")
                        }
                    } catch (cancelled: CancellationException) { throw cancelled
                    } catch (_: Exception) { mutableState.value = SessionState.RecoveryRequired("No se pudo leer la copia local.") }
                }
            }
            return
        }
        lock.withLock {
            if (version != generation || tokens.snapshot != snapshot) return
            try {
                val token = requireNotNull(snapshot.token)
                val local = token.startsWith("offline-")
                require(snapshot.localUserId == null || snapshot.localUserId == user.id) { "El v\u00ednculo local no coincide." }
                require(if(local) validVisitor(user,token) else user.remoteId != null) { "Identidad inv\u00e1lida." }
                if (snapshot.localUserId == null) tokens.save(token,user.id)
                preferences.setUsuarioActivoId(user.id)
                mutableState.value = SessionState.Authenticated(user.id,if(local) SessionSource.LOCAL_VISITOR else SessionSource.REMOTE,local)
                if (!local) startSync(version,tokens.snapshot,user.id)
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { mutableState.value = SessionState.RecoveryRequired("No se pudo guardar la sesi\u00f3n. Reintenta.") }
        }
    }

    suspend fun retryRestore() = restore()

    private fun startSync(version: Long, snapshot: SessionSnapshot, localId: Int) {
        syncJob = scope.launch {
            try { access.sincronizarCatalogoYProgreso(localId)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                lock.withLock {
                    if (version != generation || tokens.snapshot != snapshot) return@withLock
                    if (e is ApiException && e.status == 401) invalidateLocked(requireNotNull(snapshot.token))
                    else if (e.permiteFallbackOffline()) {
                        val current = mutableState.value as? SessionState.Authenticated
                        if (current?.localUserId == localId) mutableState.value = current.copy(offline = true)
                    }
                }
            }
        }
    }

    suspend fun unauthorized(rejectedToken: String) = lock.withLock { invalidateLocked(rejectedToken) }

    private suspend fun invalidateLocked(rejectedToken: String) {
        if (tokens.token != rejectedToken) return
        generation++
        try { if (!tokens.clearIfCurrent(rejectedToken)) return }
        catch (_: Exception) {
            mutableState.value = SessionState.RecoveryRequired("No se pudo invalidar la sesi\u00f3n local. Reintenta.")
            return
        }
        // Do not cancel the sync coroutine that is reporting this 401.
        mutableState.value = SessionState.SignedOut("Tu sesi\u00f3n expir\u00f3. Inicia sesi\u00f3n nuevamente.")
        try { preferences.cerrarSesion() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.value = SessionState.SignedOut("Sesi\u00f3n expirada. No se pudo limpiar la preferencia local.") }
    }

    suspend fun logout() {
        val captured = lock.withLock {
            val token = tokens.token
            generation++
            syncJob?.cancel()
            try {
                tokens.clear()
                mutableState.value = SessionState.SignedOut()
                preferences.cerrarSesion()
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { mutableState.value = SessionState.RecoveryRequired("No se pudo cerrar la sesi\u00f3n local. Reintenta.") }
            token
        }
        if (captured != null && !captured.startsWith("offline-")) {
            try { revoke(captured)
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                lock.withLock {
                    if (tokens.token == null && mutableState.value is SessionState.SignedOut)
                        mutableState.value = SessionState.SignedOut("Sesión cerrada en este dispositivo. El servidor no confirmó la revocación.")
                }
            }
        }
    }

    private fun validVisitor(user: UsuarioEntity, token: String) =
        token == "offline-visitor-${user.id}" && user.remoteId == null && user.contrasenaHash.isBlank() && user.correoInstitucional.isBlank() && user.rol == Rol.ESTUDIANTE
}
