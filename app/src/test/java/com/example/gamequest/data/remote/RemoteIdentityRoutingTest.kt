package com.example.gamequest.data.remote

import com.example.gamequest.data.model.RankingEntry
import org.junit.Assert.*
import org.junit.Test

class RemoteIdentityRoutingTest {
    @Test fun ranking_resaltaCuentaConIdsDistintos() {
        assertTrue(RankingEntry(51, null, "Current", 0, 1).isCurrent(8, 51))
        assertFalse(RankingEntry(8, null, "Other", 0, 1).isCurrent(8, 51))
        assertFalse(RankingEntry(null, 51, "Visitor", 0, 1).isCurrent(8, 51))
        assertTrue(RankingEntry(null, 8, "Visitor", 0, 1).isCurrent(8, null))
        assertNotEquals(RankingEntry(8,null,"",0,1).stableKey,RankingEntry(null,8,"",0,1).stableKey)
    }
}
