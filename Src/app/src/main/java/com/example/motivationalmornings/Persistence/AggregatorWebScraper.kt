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
                    .timeout(25_000)
                    .followRedirects(true)
                    .get()
                NewsHeadlineExtractor.extract(doc)
            }
        }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

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
