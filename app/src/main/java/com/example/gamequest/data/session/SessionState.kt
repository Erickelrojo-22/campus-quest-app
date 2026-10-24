package com.example.gamequest.data.session

sealed interface SessionState {
    data object Restoring : SessionState
    data class SignedOut(val reason: String? = null) : SessionState
    data class Authenticated(val localUserId: Int, val source: SessionSource, val offline: Boolean) : SessionState
    data class RecoveryRequired(val message: String) : SessionState
}
enum class SessionSource { REMOTE, LOCAL_VISITOR, LOCAL_ACCOUNT }
