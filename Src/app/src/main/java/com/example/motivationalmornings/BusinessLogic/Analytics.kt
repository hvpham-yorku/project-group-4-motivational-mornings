package com.example.motivationalmornings.BusinessLogic

import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
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

data class IntentionSuggestion(
    val explanation: String,
    val intention: String
)

private data class PatternKey(
    val keyword: String,
    val dayOfWeek: String?,
    val timeOfDay: String?,
    val weather: String?
)

/**
 * A helper class to encapsulate the logic for tracking intention-related analytics.
 */
class Analytics(private val analyticsRepository: AnalyticsRepository) {
    private val stopWords = setOf(
        "a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
        "from", "i", "in", "is", "it", "my", "of", "on", "or", "that",
        "the", "this", "to", "with", "will", "today", "tonight", "tomorrow",
        "would", "like", "out", "want", "going", "have", "has", "had", "just",
        "more", "some", "your", "their", "they", "them", "what", "where", "when",
        "how", "can", "could", "should", "must", "about", "very", "really", "did",
        "get", "got", "make", "take", "come", "give", "look", "back", "into", "than"
    )

    // Expanded list of valid words to improve keyword extraction and filter typos.
    private val dictionary = setOf(
        // Verbs
        "run", "read", "book", "meditate", "exercise", "walk", "study", "work",
        "clean", "cook", "water", "sleep", "stretch", "focus", "learn", "write",
        "journal", "coding", "gym", "yoga", "breathe", "task", "plan", "start",
        "finish", "complete", "listen", "speak", "eat", "drink", "hydrate", "hike",
        "paint", "draw", "sing", "dance", "play", "help", "save", "build", "create",
        "meditating", "running", "walking", "working", "learning", "cleaning",
        "cooking", "sleeping", "focusing", "writing", "reading", "coding", "starting",
        "shovel", "wear",
        
        // Nouns
        "productivity", "meditation", "morning", "journaling", "workout", "nature",
        "friends", "family", "peace", "calm", "happy", "smile", "present", "goal",
        "breath", "fruit", "veggies", "bike", "cycle", "project", "meeting", "class",
        "break", "rest", "energy", "time", "day", "week", "month", "year", "mind",
        "body", "soul", "heart", "health", "life", "dream", "success", "growth",
        "snow", "coat",
        
        // Adjectives
        "healthy", "productive", "active", "strong", "patient", "helpful", "creative",
        "grateful", "kind", "happy", "positive", "calm", "peaceful", "mindful",
        "amazing", "great", "better", "best", "new", "fresh", "daily", "early"
    )

    /**
     * Tracks the event of an intention being set.
     */
    suspend fun trackIntentionSet(intention: String, imageResId: Int, weather: String?) {
        val extractedKeywords = extractKeywords(intention)
        val event = IntentionAnalyticsEvent(
            intention = intention,
            timestamp = Date(),
            weather = weather ?: "Unknown",
            imageResId = imageResId,
            keywords = extractedKeywords
        )
        analyticsRepository.trackIntentionSet(event)
    }

    fun extractKeywords(intention: String): List<String> {
        return intention
            .lowercase()
            .split(Regex("[^a-z0-9]+"))
            .asSequence()
            .filter { it.length >= 3 }
            .filterNot { stopWords.contains(it) }
            // Filter to include words that are likely correct and relevant
            .filter { isValidWord(it) }
            .distinct()
            .toList() // Removed take(5) to allow more keywords if available
    }

    /**
     * Checks if a word is likely not a typo and is relevant.
     */
    private fun isValidWord(word: String): Boolean {
        // Direct match in our expanded dictionary
        if (dictionary.contains(word)) return true
        
        // Basic check for plurals or common endings of words in our dictionary
        if (word.endsWith("s") && dictionary.contains(word.dropLast(1))) return true
        if (word.endsWith("es") && dictionary.contains(word.dropLast(2))) return true
        if (word.endsWith("ing") && dictionary.contains(word.dropLast(3))) return true
        if (word.endsWith("ed") && dictionary.contains(word.dropLast(2))) return true
        
        // If it's a longer word (6+) and has a reasonable vowel/consonant balance, 
        // we might allow it even if not in our dictionary to avoid being too restrictive.
        // This helps catch valid words we haven't hardcoded while still filtering short typos.
        if (word.length >= 6) {
            val vowels = word.count { it in "aeiou" }
            val consonants = word.length - vowels
            // Simple heuristic to filter out "gibberish" typos (e.g., "sdfghj")
            if (vowels > 0 && consonants > 0 && vowels.toDouble() / word.length > 0.15) {
                return true
            }
        }
        
        return false
    }

    /**
     * Detects recurring keyword × context combinations from saved intentions (same rules as the analytics dashboard).
     */
    fun detectIntentionPatterns(intentions: List<Intention>): List<IntentionPattern> {
        val combinations = mutableMapOf<PatternKey, Int>()

        intentions.forEach { intention ->
            val keywords = extractKeywords(intention.text)
            val dayOfWeek = dayOfWeekLabel(intention.date)
            val timeBucket = hourBucketFromSavedTime(intention.time)
            val weatherBucket = intention.weather?.let { groupWeather(it) }

            keywords.forEach { keyword ->
                val dayTimeKey = PatternKey(keyword, dayOfWeek, timeBucket, null)
                combinations[dayTimeKey] = combinations.getOrDefault(dayTimeKey, 0) + 1

                val weatherKey = PatternKey(keyword, null, null, weatherBucket)
                combinations[weatherKey] = combinations.getOrDefault(weatherKey, 0) + 1

                val fullKey = PatternKey(keyword, dayOfWeek, timeBucket, weatherBucket)
                combinations[fullKey] = combinations.getOrDefault(fullKey, 0) + 1
            }
        }

        return combinations.filter { it.value > 1 }
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
            .take(10)
    }

    /**
     * Suggested intention strings based on [detectIntentionPatterns], ranked for the current day, time, and optional weather.
     */
    fun suggestIntentionsFromPatterns(
        allIntentions: List<Intention>,
        currentWeatherDisplay: String?,
        alreadyTodayTexts: List<String>
    ): List<IntentionSuggestion> {
        if (allIntentions.isEmpty()) return emptyList()

        val today = todayDayOfWeekLabel()
        val nowBucket = currentHourBucket()
        val weatherBucket = currentWeatherDisplay?.let { groupWeather(it) }

        val patterns = detectIntentionPatterns(allIntentions)
        val ranked = rankPatternsForContext(patterns, today, nowBucket, weatherBucket)
        val skip = alreadyTodayTexts.map { it.trim().lowercase(Locale.getDefault()) }.toSet()

        val out = mutableListOf<IntentionSuggestion>()
        val seenIntentions = mutableSetOf<String>()

        for (pattern in ranked) {
            if (out.size >= 3) break
            val explanation = explanationForPattern(pattern)
            val intention = intentionFromPattern(pattern)
            
            if (explanation.isNotBlank() && intention.trim().lowercase(Locale.getDefault()) !in skip && intention !in seenIntentions) {
                out.add(IntentionSuggestion(explanation, intention))
                seenIntentions.add(intention)
            }
        }

        if (out.size < 3) {
            val topKeywords = allIntentions
                .flatMap { extractKeywords(it.text) }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .map { it.key }
                .distinct()
            
            for (kw in topKeywords) {
                if (out.size >= 3) break
                val intention = intentionFromKeyword(kw)
                if (intention.lowercase(Locale.getDefault()) !in skip && intention !in seenIntentions) {
                    out.add(IntentionSuggestion(fallbackTemplateForKeyword(kw), intention))
                    seenIntentions.add(intention)
                }
            }
        }

        return out
    }

    private fun explanationForPattern(pattern: IntentionPattern): String {
        val kw = pattern.keyword.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        
        return when {
            // Specific examples provided by user
            pattern.keyword.lowercase() == "shovel" && pattern.weather?.contains("Snow", ignoreCase = true) == true ->
                "You often shovel snow around this time."
            pattern.keyword.lowercase() == "coat" && pattern.weather != null ->
                "At this temperature you wear a coat."
                
            // General patterns
            pattern.dayOfWeek != null && pattern.timeOfDay != null ->
                "You usually focus on $kw on ${pattern.dayOfWeek}s at ${pattern.timeOfDay}."
            pattern.dayOfWeek != null ->
                "You often think about $kw on ${pattern.dayOfWeek}s."
            pattern.timeOfDay != null ->
                "Around ${pattern.timeOfDay}, you frequently set intentions for $kw."
            pattern.weather != null ->
                "When it's ${pattern.weather}, you typically $kw."
            else -> fallbackTemplateForKeyword(pattern.keyword)
        }
    }

    private fun intentionFromPattern(pattern: IntentionPattern): String {
        return when (val kw = pattern.keyword.lowercase(Locale.getDefault())) {
            "shovel" -> "Today I want to shovel snow"
            "coat" -> "Today I'll remember my coat"
            else -> intentionFromKeyword(kw)
        }
    }

    private fun intentionFromKeyword(keyword: String): String {
        val word = keyword.lowercase(Locale.getDefault())
        return when (word) {
            "run" -> "Today I want to go for a run"
            "read" -> "Today I want to read my book"
            "meditate" -> "Today I want to meditate"
            "work" -> "Today I want to focus on work"
            "exercise" -> "Today I want to exercise"
            "walk" -> "Today I want to go for a walk"
            "study" -> "Today I want to study"
            "clean" -> "Today I want to clean"
            "journal" -> "Today I want to journal"
            else -> {
                val capitalized = keyword.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                "Today I want to focus on $capitalized"
            }
        }
    }

    private fun rankPatternsForContext(
        patterns: List<IntentionPattern>,
        today: String,
        nowBucket: String,
        weatherBucket: String?
    ): List<IntentionPattern> {
        if (patterns.isEmpty()) return emptyList()

        fun matchesTier1(p: IntentionPattern): Boolean {
            if (p.dayOfWeek != null && p.dayOfWeek != today) return false
            if (p.timeOfDay != null && p.timeOfDay != nowBucket) return false
            if (p.weather != null && weatherBucket != null && p.weather != weatherBucket) return false
            return true
        }

        val tier1 = patterns.filter(::matchesTier1).sortedByDescending { it.count }
        if (tier1.isNotEmpty()) return tier1

        val tier2 = patterns.filter { p ->
            (p.dayOfWeek == null || p.dayOfWeek == today) &&
                (p.timeOfDay == null || p.timeOfDay == nowBucket)
        }.sortedByDescending { it.count }
        if (tier2.isNotEmpty()) return tier2

        return patterns.sortedByDescending { it.count }
    }

    private fun fallbackTemplateForKeyword(keyword: String): String {
        val word = keyword.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        return "Lean into $word today — it's been a recurring theme for you."
    }

    private fun todayDayOfWeekLabel(): String =
        LocalDate.now().dayOfWeek.name.lowercase(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    private fun dayOfWeekLabel(isoDate: String): String? = try {
        LocalDate.parse(isoDate).dayOfWeek.name.lowercase(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    } catch (_: Exception) {
        null
    }

    private fun hourBucketFromSavedTime(time: String?): String? {
        val hourPart = time?.split(":")?.firstOrNull() ?: return null
        return "${hourPart}:00"
    }

    private fun currentHourBucket(): String {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val hourPart = LocalTime.now().format(fmt).substringBefore(':')
        return "$hourPart:00"
    }

    /**
     * Same bucketing as the analytics dashboard so pattern keys align with live weather strings.
     */
    fun groupWeather(weather: String): String {
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

            "$condition, $bucketStart to $bucketEnd°C"
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
}
