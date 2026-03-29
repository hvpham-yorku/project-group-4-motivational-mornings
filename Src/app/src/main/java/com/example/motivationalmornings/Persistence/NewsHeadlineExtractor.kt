package com.example.motivationalmornings.Persistence

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URL

/**
 * Pulls **article** headline links from section pages (e.g. a world news index).
 * Category/navigation URLs are filtered out using path shape and title heuristics.
 */
object NewsHeadlineExtractor {

    private const val MaxHeadlines = 40

    /** e.g. /2024/6/9/... or /2024/06/09/... */
    private val DateInPath = Regex("""/20\d{2}/\d{1,2}/\d{1,2}/""")

    private val NavNoiseInPath = listOf(
        "/tag/", "/tags/", "/topic/", "/topics/", "/category/", "/categories/",
        "/section/", "/sections/", "/profile/", "/account/", "/login", "/subscribe",
        "/newsletter", "/videos", "/video/", "/live/", "/watch/", "/shows/",
        "/podcasts/", "/collection/", "/authors/", "/search", "?page=",
    )

    /** Link text that is usually a section name, not a story headline. */
    private val CategoryLikeTitles = setOf(
        "world", "u.s.", "us", "politics", "business", "tech", "technology",
        "science", "health", "entertainment", "sport", "sports", "travel", "style",
        "opinion", "opinions", "weather", "markets", "africa", "asia", "europe",
        "americas", "middle east", "china", "india", "uk", "europe",
        "latest", "more", "watch", "listen", "live", "video", "photos", "audio",
        "climate", "cnn profiles", "cnn underscored", "cnn values", "fast facts",
    )

    private val StructuredSelectors = listOf(
        "article a[href]",
        "h2 a[href]",
        "h3 a[href]",
        "h4 a[href]",
        "li a[href]", // Added to support list-heavy layouts like FotMob
        "div[class*='headline'] a[href]",
        "div[class*='title'] a[href]",
    )

    fun extract(doc: Document): List<AggregatorArticle> {
        val baseUri = doc.baseUri().ifBlank { "https://localhost" }
        val seen = linkedMapOf<String, String>()

        for (selector in StructuredSelectors) {
            doc.select(selector).forEach { anchor ->
                considerAnchor(seen, anchor, baseUri)
            }
            if (seen.size >= 25) break // Increased threshold
        }

        if (seen.size < 10) { // Increased threshold to fall back to general links
            doc.select("a[href]").forEach { anchor ->
                considerAnchor(seen, anchor, baseUri)
            }
        }

        return seen.entries
            .map { AggregatorArticle(title = it.value, url = it.key) }
            .take(MaxHeadlines)
    }

    private fun considerAnchor(seen: LinkedHashMap<String, String>, anchor: Element, baseUri: String) {
        val url = absoluteLink(anchor, baseUri) ?: return
        if (!isLikelyArticleLink(url)) return
        val title = anchor.text().trim().replace(Regex("\\s+"), " ")
        if (!isPlausibleHeadlineText(title, url)) return
        seen.putIfAbsent(url, title)
    }

    internal fun absoluteLink(anchor: Element, baseUri: String): String? {
        val abs = anchor.absUrl("href").trim()
        if (abs.isNotEmpty()) return abs
        val raw = anchor.attr("href").trim()
        if (raw.isEmpty()) return null
        return try {
            URL(URL(baseUri), raw).toString()
        } catch (_: Exception) {
            null
        }
    }

    internal fun isPlausibleHeadlineText(text: String, articleUrl: String): Boolean {
        if (text.length < 12 || text.length > 320) return false // Lowered minimum length slightly
        val lower = text.lowercase().trim()
        val banned = setOf(
            "watch", "listen", "sign in", "log in", "subscribe", "cookies", "ad choices",
            "follow us", "terms of use", "privacy policy", "skip to content", "accessibility",
        )
        if (lower in banned) return false
        if (text.all { it.isDigit() || it.isWhitespace() || it == ':' || it == '|' }) return false

        if (lower in CategoryLikeTitles) return false

        val words = lower.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val path = runCatching { URL(articleUrl).path }.getOrNull().orEmpty()
        val hasStrongArticleUrl = hasArticleUrlSignals(path)
        
        // Real headlines almost always have several words; category chips often have 1–2.
        if (words.size < 2 && text.length < 40 && !hasStrongArticleUrl) return false // Lowered word count check

        // All-caps short lines are often nav, not headlines
        if (text.length <= 30 && text == text.uppercase() && words.size <= 4) return false

        return true
    }

    internal fun isLikelyArticleLink(url: String): Boolean {
        val parsed = try {
            URL(url)
        } catch (_: Exception) {
            return false
        }
        val protocol = parsed.protocol.lowercase()
        if (protocol != "http" && protocol != "https") return false

        val lower = url.lowercase()
        val noiseFragment = listOf(
            "javascript:", "mailto:", "tel:", "#/lite/",
            ".jpg", ".jpeg", ".png", ".gif", ".svg", ".webp", ".pdf", ".mp4",
            "doubleclick", "googletagmanager", "facebook.com/sharer",
            "twitter.com/intent", "platform.twitter.com",
        )
        if (noiseFragment.any { lower.contains(it) }) return false

        if (NavNoiseInPath.any { lower.contains(it) }) return false

        val path = parsed.path.trim('/').lowercase()
        if (path.length < 4) return false // Lowered for shorter news paths

        val segments = path.split('/').filter { it.isNotEmpty() }
        if (segments.isEmpty()) return false

        if (isLikelyCategoryOnlyPath(path, segments)) return false

        return hasArticleUrlSignals(parsed.path)
    }

    /** Section home links: /world, /us/politics, /business/tech with no story slug. */
    internal fun isLikelyCategoryOnlyPath(path: String, segments: List<String>): Boolean {
        val lowerPath = path.lowercase()
        if (segments.size == 1) {
            val s = segments[0]
            // One long hyphenated slug is almost always an article, not a section
            if (s.length >= 22 && s.count { it == '-' } >= 2) return false
            if (s.length <= 18 && s.matches(Regex("^[a-z0-9-]+$")) && !s.any { it.isDigit() }) {
                if (!s.contains('-')) return true
                // Short hyphenated hub: e.g. middle-east, us-politics (still often categories)
                if (s.length <= 16 && s.split('-').all { it.length <= 12 }) return true
            }
        }
        if (segments.size == 2) {
            val a = segments[0]
            val b = segments[1]
            val shortBoth = a.length <= 14 && b.length <= 14
            val noStorySignals = !DateInPath.containsMatchIn(lowerPath) &&
                !b.contains('-') &&
                b.none { it.isDigit() }
            if (shortBoth && noStorySignals) return true
        }
        return false
    }

    internal fun hasArticleUrlSignals(rawPath: String): Boolean {
        val path = rawPath.lowercase()
        if (DateInPath.containsMatchIn(path)) return true

        val segments = path.trim('/').split('/').filter { it.isNotEmpty() }
        if (segments.isEmpty()) return false
        val last = segments.last()

        // Long hyphenated slug (typical article URL)
        if (last.length >= 18 && last.count { it == '-' } >= 2) return true // Relaxed constraints
        if (last.length >= 28 && last.contains('-')) return true

        // /story/, /articles/, /news/story-…
        if (Regex("""/(story|stories|article|articles|news)/.+""").containsMatchIn(path) && last.length >= 8) {
            return true
        }

        // Trailing long numeric id (common on wire services / some CMSs)
        if (Regex("""-\d{5,}(/?.*)?$""").containsMatchIn(path)) return true
        if (Regex("""/\d{6,}/?$""").containsMatchIn(path)) return true

        // BBC-style or general news: world-europe-12345678
        if (Regex("""^[a-z0-9]+(-[a-z0-9]+){1,}-\d{4,}$""").matches(last)) return true

        // Meaty final segment
        if (segments.size >= 2 && last.contains('-') && last.length >= 15) return true

        return true // Default to true if it passed noise filters and has some length
    }
}
