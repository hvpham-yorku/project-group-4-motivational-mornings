package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.Intention
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

data class IntentionPattern(
    val keyword: String,
    val dayOfWeek: String? = null,
    val timeOfDay: String? = null,
    val weather: String? = null,
    val count: Int
)

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
            .map { groupWeather(it.weather!!) }
            .groupingBy { it }
            .eachCount()

        val dateCounts = intentions.groupingBy { it.date }
            .eachCount()
            .toSortedMap()

        val patterns = detectPatterns(intentions)

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

    private fun detectPatterns(intentions: List<Intention>): List<IntentionPattern> {
        val combinations = mutableMapOf<PatternKey, Int>()

        intentions.forEach { intention ->
            val keywords = analytics.extractKeywords(intention.text)
            val dayOfWeek = try {
                LocalDate.parse(intention.date).dayOfWeek.name.lowercase(Locale.getDefault())
                    .replaceFirstChar { it.titlecase(Locale.getDefault()) }
            } catch (e: Exception) {
                null
            }
            val timeBucket = intention.time?.split(":")?.firstOrNull()?.let { "$it:00" }
            val weatherBucket = intention.weather?.let { groupWeather(it) }

            keywords.forEach { keyword ->
                // We look for patterns linking keyword with day, time, and weather.
                // We track various combinations to find significant ones.
                
                // 1. Keyword + Day + Time (as requested in the example)
                val dayTimeKey = PatternKey(keyword, dayOfWeek, timeBucket, null)
                combinations[dayTimeKey] = combinations.getOrDefault(dayTimeKey, 0) + 1

                // 2. Keyword + Weather
                val weatherKey = PatternKey(keyword, null, null, weatherBucket)
                combinations[weatherKey] = combinations.getOrDefault(weatherKey, 0) + 1
                
                // 3. Keyword + Day + Time + Weather (Full correlation)
                val fullKey = PatternKey(keyword, dayOfWeek, timeBucket, weatherBucket)
                combinations[fullKey] = combinations.getOrDefault(fullKey, 0) + 1
            }
        }

        return combinations.filter { it.value > 1 } // Only report patterns occurring more than once
            .map { (key, count) ->
                IntentionPattern(
                    keyword = key.keyword,
                    dayOfWeek = key.dayOfWeek,
                    timeOfDay = key.timeOfDay,
                    weather = key.weather,
                    count = count
                )
            }
            .sortedByDescending { it.count }
            .take(10) // Limit to top 10 most frequent patterns
    }

    private data class PatternKey(
        val keyword: String,
        val dayOfWeek: String?,
        val timeOfDay: String?,
        val weather: String?
    )

    /**
     * Groups weather strings like "Clear, 3.4°C" into buckets like "Clear, 2-4°C".
     * Stored weather uses [Double] Celsius from the API; decimals must be parsed so
     * near-identical readings merge. Condition text is normalized (whitespace + casing)
     * so the same condition shares one bucket. A 3°C-wide band groups values within
     * two degrees of each other (e.g. 2°C and 4°C).
     */
    private fun groupWeather(weather: String): String {
        val regex = Regex("""^(.+),\s*(-?\d+(?:\.\d+)?)°C$""")
        val match = regex.find(weather.trim())
        return if (match != null) {
            val condition = normalizeConditionLabel(match.groupValues[1])
            val temp = match.groupValues[2].toDouble().roundToInt()

            val bucketSize = 3
            val bucketStart = if (temp >= 0) {
                ((temp - 2) / bucketSize) * bucketSize + 2
            } else {
                (floor((temp.toDouble() - 2) / bucketSize).toInt() * bucketSize) + 2
            }
            val bucketEnd = bucketStart + (bucketSize - 1)

            "$condition, $bucketStart-$bucketEnd°C"
        } else {
            weather.trim()
        }
    }

    private fun normalizeConditionLabel(raw: String): String {
        val collapsed = raw.trim().replace(Regex("\\s+"), " ")
        return collapsed.split(' ')
            .joinToString(" ") { word ->
                word.lowercase(Locale.getDefault()).replaceFirstChar { c ->
                    c.titlecase(Locale.getDefault())
                }
            }
    }

    fun getIntentionsByKeyword(keyword: String): List<Intention> {
        return uiState.value.allIntentions.filter { intention ->
            analytics.extractKeywords(intention.text).contains(keyword.lowercase())
        }
    }
}
