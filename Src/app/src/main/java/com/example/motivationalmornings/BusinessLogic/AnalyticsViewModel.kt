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
    val weatherDistribution: Map<String, Int> = emptyMap(),
    val intentionsByDate: Map<String, Int> = emptyMap()
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

        val allKeywords = intentions.flatMap { analytics.extractKeywords(it.text) }
        val keywordCounts = allKeywords.groupingBy { it }.eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(10)

        val weatherCounts = intentions.filter { it.weather != null }
            .groupingBy { it.weather!! }
            .eachCount()

        val dateCounts = intentions.groupingBy { it.date }
            .eachCount()
            .toSortedMap()

        return AnalyticsState(
            totalIntentions = intentions.size,
            topKeywords = keywordCounts,
            weatherDistribution = weatherCounts,
            intentionsByDate = dateCounts
        )
    }
}
