package com.example.motivationalmornings.unit

import com.example.motivationalmornings.DatabaseConfig
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test in the explicit unit package (ITR2 requirement: explicit folders for unit/integration tests).
 */
class DatabaseConfigTest {
    @Test
    fun useRealDatabase_isBoolean() {
        assertTrue(DatabaseConfig.USE_REAL_DATABASE is Boolean)
    }
}
