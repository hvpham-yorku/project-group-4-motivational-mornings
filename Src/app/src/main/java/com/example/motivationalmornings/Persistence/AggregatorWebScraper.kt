package com.example.motivationalmornings.Persistence

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

fun interface AggregatorWebScraper {
    suspend fun scrapeHeadlines(pageUrl: String): Result<List<AggregatorArticle>>
}

class DefaultAggregatorWebScraper(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AggregatorWebScraper {

    override suspend fun scrapeHeadlines(pageUrl: String): Result<List<AggregatorArticle>> =
        withContext(ioDispatcher) {
            runCatching {
                val normalized = normalizePageUrl(pageUrl)
                if (normalized.isEmpty()) {
                    throw IllegalArgumentException("Enter a page URL")
                }
                val doc = Jsoup.connect(normalized)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .timeout(30_000)
                    .followRedirects(true)
                    .get()
                NewsHeadlineExtractor.extract(doc)
            }
        }

    companion object {
        // Using a modern Desktop User Agent which often avoids "app-only" or simplified mobile-only redirects
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        internal fun normalizePageUrl(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return ""
            return when {
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed
                else -> "https://$trimmed"
            }
        }
    }
}
