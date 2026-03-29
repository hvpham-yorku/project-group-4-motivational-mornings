package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsTest {

    @Test
    fun trackIntentionSet_extractsKeywordsFromIntention() = runTest {
        val repository = CapturingAnalyticsRepository()
        val analytics = Analytics(repository)

        analytics.trackIntentionSet(
            intention = "I want to run and read a book on productivity today",
            imageResId = 42,
            weather = "Sunny"
        )

        val trackedEvent = repository.lastEvent
        assertNotNull(trackedEvent)
        // Check if expected keywords are present. The extraction logic filters based on a dictionary and length.
        val keywords = trackedEvent!!.keywords
        assertTrue(keywords.contains("run"))
        assertTrue(keywords.contains("read"))
        assertTrue(keywords.contains("book"))
        assertTrue(keywords.contains("productivity"))
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
        assertTrue(trackedEvent.keywords.contains("morning"))
        assertTrue(trackedEvent.keywords.contains("stretch"))
        assertTrue(trackedEvent.keywords.contains("meditation"))
    }

    private class CapturingAnalyticsRepository : AnalyticsRepository {
        var lastEvent: IntentionAnalyticsEvent? = null

        override suspend fun trackIntentionSet(event: IntentionAnalyticsEvent) {
            lastEvent = event
        }
    }
}
