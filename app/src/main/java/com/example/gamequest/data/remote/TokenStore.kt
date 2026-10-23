package com.example.gamequest.data.remote

interface TokenStore {
    val token: String?
    fun save(value: String)
    fun clear()
}
