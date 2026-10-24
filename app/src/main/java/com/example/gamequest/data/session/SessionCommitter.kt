package com.example.gamequest.data.session

fun interface SessionCommitter {
    suspend fun commitAuthenticated(token: String, localUserId: Int)
}
