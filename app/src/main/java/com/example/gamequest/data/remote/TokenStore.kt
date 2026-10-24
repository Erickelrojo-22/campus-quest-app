package com.example.gamequest.data.remote

data class SessionSnapshot(val token: String?, val localUserId: Int? = null)
interface TokenStore {
    val snapshot: SessionSnapshot
    val token: String? get() = snapshot.token
    fun save(value: String, localUserId: Int? = null)
    fun clear()
    fun clearIfCurrent(expectedToken: String): Boolean
}
