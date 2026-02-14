package com.example.motivationalmornings.data

import java.util.Date

// Data class to hold the analytics event information
data class IntentionAnalyticsEvent(
    val intention: String,
    val timestamp: Date,
    val weather: String, // For now, a simple string. Can be a more complex object later.
    val imageResId: Int
)

// Interface for our analytics repository
interface AnalyticsRepository {
    suspend fun trackIntentionSet(event: IntentionAnalyticsEvent)
}

// A fake implementation that just prints the event for now.
class FakeAnalyticsRepository : AnalyticsRepository {
    override suspend fun trackIntentionSet(event: IntentionAnalyticsEvent) {
        // In a real implementation, this would send the event to an analytics service.
        println("Analytics event tracked: $event")
    }
}
