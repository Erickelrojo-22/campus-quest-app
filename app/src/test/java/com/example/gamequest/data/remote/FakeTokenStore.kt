package com.example.gamequest.data.remote

import java.io.IOException

class FakeTokenStore(initial: String? = null) : TokenStore {
    override var snapshot = SessionSnapshot(initial)
        private set
    val saved = mutableListOf<String>()
    var failRemoteSave = false
    override fun save(value: String, localUserId: Int?) {
        if (failRemoteSave && !value.startsWith("offline-")) throw IOException("disk unavailable")
        saved += value
        snapshot = SessionSnapshot(value, localUserId)
    }
    override fun clearIfCurrent(expectedToken: String): Boolean { if (token != expectedToken) return false; clear(); return true }
    override fun clear() { snapshot = SessionSnapshot(null) }
}
