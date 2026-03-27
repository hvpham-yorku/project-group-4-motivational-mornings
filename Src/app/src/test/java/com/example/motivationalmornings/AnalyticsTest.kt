package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnalyticsTest {

    @Test
    fun trackIntentionSet_extractsKeywordsFromIntention() = runTest {
        val repository = CapturingAnalyticsRepository()
        val analytics = Analytics(repository)

        analytics.trackIntentionSet(
            intention = "I would like to run 5k and read a book on productivity out today",
            imageResId = 42,
            weather = "Sunny"
        )

        val trackedEvent = repository.lastEvent
        assertNotNull(trackedEvent)
        // "would", "like", "out" should be filtered out now
        assertEquals(listOf("run", "read", "book", "productivity"), trackedEvent!!.keywords)
    }

    @Test
    fun trackIntentionSet_defaultsWeatherToUnknown() = runTest {
        val repository = CapturingAnalyticsRepository()
        val analytics = Analytics(repository)

        analytics.trackIntentionSet(
            intention = "Morning stretch and meditation",
            imageResId = 7,
            weather = null
        )

        val trackedEvent = repository.lastEvent
        assertNotNull(trackedEvent)
        assertEquals("Unknown", trackedEvent!!.weather)
        assertEquals(listOf("morning", "stretch", "meditation"), trackedEvent.keywords)
    }

    private class CapturingAnalyticsRepository : AnalyticsRepository {
        var lastEvent: IntentionAnalyticsEvent? = null

        override suspend fun trackIntentionSet(event: IntentionAnalyticsEvent) {
            lastEvent = event
        }
    }
}
