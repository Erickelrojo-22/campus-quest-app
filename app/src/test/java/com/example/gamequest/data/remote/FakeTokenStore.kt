package com.example.gamequest.data.remote

import java.io.IOException

class FakeTokenStore(initial: String? = null) : TokenStore {
    override var token: String? = initial
        private set
    val saved = mutableListOf<String>()
    var failRemoteSave = false
    override fun save(value: String) {
        if (failRemoteSave && !value.startsWith("offline-")) throw IOException("disk unavailable")
        saved += value
        token = value
    }
    override fun clear() { token = null }
}
