package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.Intention
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AnalyticsState(
    val totalIntentions: Int = 0,
    val topKeywords: List<Pair<String, Int>> = emptyList(),
    val allKeywords: List<Pair<String, Int>> = emptyList(),
    val weatherDistribution: Map<String, Int> = emptyMap(),
    val intentionsByDate: Map<String, Int> = emptyMap(),
    val allIntentions: List<Intention> = emptyList(),
    val detectedPatterns: List<IntentionPattern> = emptyList()
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
            .map { analytics.groupWeather(it.weather!!) }
            .groupingBy { it }
            .eachCount()

        val dateCounts = intentions.groupingBy { it.date }
            .eachCount()
            .toSortedMap()

        val patterns = analytics.detectIntentionPatterns(intentions)

        return AnalyticsState(
            totalIntentions = intentions.size,
            topKeywords = top6Keywords,
            allKeywords = keywordCounts,
            weatherDistribution = weatherCounts,
            intentionsByDate = dateCounts,
            allIntentions = intentions,
            detectedPatterns = patterns
        )
    }

    fun getIntentionsByKeyword(keyword: String): List<Intention> {
        return uiState.value.allIntentions.filter { intention ->
            analytics.extractKeywords(intention.text).contains(keyword.lowercase())
        }
    }
}
