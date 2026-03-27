package com.example.motivationalmornings.BusinessLogic

import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import java.util.Date

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
        
        // Nouns
        "productivity", "meditation", "morning", "journaling", "workout", "nature",
        "friends", "family", "peace", "calm", "happy", "smile", "present", "goal",
        "breath", "fruit", "veggies", "bike", "cycle", "project", "meeting", "class",
        "break", "rest", "energy", "time", "day", "week", "month", "year", "mind",
        "body", "soul", "heart", "health", "life", "dream", "success", "growth",
        
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
}
