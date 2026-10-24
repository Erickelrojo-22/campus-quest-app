package com.example.gamequest.data.session

/** Commits a persisted, non-network local account session. */
fun interface LocalAccountSessionCommitter {
    suspend fun commitLocalAccount(localUserId: Int)
}
