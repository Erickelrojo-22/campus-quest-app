package com.example.gamequest.data.remote

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*

class SessionTokenStoreTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    @Before fun setup() { SessionTokenStore(context).clear() }
    @After fun cleanup() { SessionTokenStore(context).clear() }
    @Test fun snapshotPersisteVinculoYRechazaInvalidacionAntigua() {
        SessionTokenStore(context).save("fixture-token",8)
        val restored=SessionTokenStore(context)
        assertEquals(SessionSnapshot("fixture-token",8),restored.snapshot)
        assertFalse(restored.clearIfCurrent("previous-token"))
        assertEquals(8,restored.snapshot.localUserId)
        assertTrue(restored.clearIfCurrent("fixture-token"))
        assertEquals(SessionSnapshot(null),SessionTokenStore(context).snapshot)
    }
    @Test fun tokenCifradoAntiguo_sigueSinVinculo() {
        SessionTokenStore(context).save("legacy-fixture",8)
        check(context.getSharedPreferences("remote_session",Context.MODE_PRIVATE).edit().remove("localUserId").commit())
        assertEquals(SessionSnapshot("legacy-fixture",null),SessionTokenStore(context).snapshot)
    }
}
