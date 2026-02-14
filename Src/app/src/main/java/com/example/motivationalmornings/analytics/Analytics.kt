package com.example.motivationalmornings.analytics

import com.example.motivationalmornings.data.AnalyticsRepository
import com.example.motivationalmornings.data.IntentionAnalyticsEvent
import java.util.Date

/**
 * A helper class to encapsulate the logic for tracking intention-related analytics.
 */
class Analytics(private val analyticsRepository: AnalyticsRepository) {

    /**
     * Tracks the event of an intention being set.
     */
    suspend fun trackIntentionSet(intention: String, imageResId: Int) {
        val event = IntentionAnalyticsEvent(
            intention = intention,
            timestamp = Date(),
            weather = "Sunny", // Dummy data for now
            imageResId = imageResId
        )
        analyticsRepository.trackIntentionSet(event)
    }
}
