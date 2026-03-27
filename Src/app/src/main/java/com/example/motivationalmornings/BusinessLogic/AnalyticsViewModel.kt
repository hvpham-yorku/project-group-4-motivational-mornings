package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.Intention
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.floor

data class AnalyticsState(
    val totalIntentions: Int = 0,
    val topKeywords: List<Pair<String, Int>> = emptyList(),
    val allKeywords: List<Pair<String, Int>> = emptyList(),
    val weatherDistribution: Map<String, Int> = emptyMap(),
    val intentionsByDate: Map<String, Int> = emptyMap(),
    val allIntentions: List<Intention> = emptyList()
)

class AnalyticsViewModel(
    private val contentRepository: ContentRepository,
    private val analytics: Analytics
) : ViewModel() {

    val uiState: StateFlow<AnalyticsState> = contentRepository.getAllIntentions()
        .map { intentions ->
            calculateAnalytics(intentions)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsState()
        )

    private fun calculateAnalytics(intentions: List<Intention>): AnalyticsState {
        if (intentions.isEmpty()) return AnalyticsState()

        val allExtractedKeywords = intentions.flatMap { analytics.extractKeywords(it.text) }
        val keywordCounts = allExtractedKeywords.groupingBy { it }.eachCount()
            .toList()
            .sortedByDescending { it.second }
        
        val top6Keywords = keywordCounts.take(6)

        // Grouping weather in the ViewModel (Presentation Layer)
        // This ensures the database (Backend) still holds the original precise values.
        val weatherCounts = intentions.filter { it.weather != null }
            .map { groupWeather(it.weather!!) }
            .groupingBy { it }
            .eachCount()

        val dateCounts = intentions.groupingBy { it.date }
            .eachCount()
            .toSortedMap()

        return AnalyticsState(
            totalIntentions = intentions.size,
            topKeywords = top6Keywords,
            allKeywords = keywordCounts,
            weatherDistribution = weatherCounts,
            intentionsByDate = dateCounts,
            allIntentions = intentions
        )
    }

    /**
     * Groups weather strings like "Clear, 3°C" into buckets like "Clear, 2-3°C".
     * A 2-degree range is used: [0-1], [2-3], [4-5], etc.
     */
    private fun groupWeather(weather: String): String {
        val regex = Regex("""^(.+),\s*(-?\d+)°C$""")
        val match = regex.find(weather)
        return if (match != null) {
            val condition = match.groupValues[1].trim()
            val temp = match.groupValues[2].toInt()
            
            // Calculate bucket: 2-degree ranges
            // e.g., 0 and 1 -> 0; 2 and 3 -> 2; 4 and 5 -> 4
            val bucketStart = if (temp >= 0) {
                (temp / 2) * 2
            } else {
                floor((temp.toDouble()) / 2).toInt() * 2
            }
            val bucketEnd = bucketStart + 1
            "$condition, $bucketStart-$bucketEnd°C"
        } else {
            weather
        }
    }

    fun getIntentionsByKeyword(keyword: String): List<Intention> {
        return uiState.value.allIntentions.filter { intention ->
            analytics.extractKeywords(intention.text).contains(keyword.lowercase())
        }
    }
}
