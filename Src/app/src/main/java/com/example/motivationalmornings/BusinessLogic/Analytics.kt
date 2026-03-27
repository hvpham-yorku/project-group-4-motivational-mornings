package com.example.motivationalmornings.BusinessLogic

import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import java.util.Date

/**
 * A helper class to encapsulate the logic for tracking intention-related analytics.
 */
class Analytics(private val analyticsRepository: AnalyticsRepository) {

    /**
     * Tracks the event of an intention being set.
     */
    suspend fun trackIntentionSet(intention: String, imageResId: Int, weather: String?) {
        val event = IntentionAnalyticsEvent(
            intention = intention,
            timestamp = Date(),
            weather = weather ?: "Unknown",
            imageResId = imageResId
        )
        analyticsRepository.trackIntentionSet(event)
    }
}
