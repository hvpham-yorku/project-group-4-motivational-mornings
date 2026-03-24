package com.example.motivationalmornings.data

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URL

/**
 * Pulls headline-style links from an HTML document (news section pages like
 * "https://www.cnn.com/world"): prominent article anchors first, then a broader pass.
 */
object NewsHeadlineExtractor {

    private const val MaxHeadlines = 40
    private val StructuredSelectors = listOf(
        "article a[href]",
        "h2 a[href]",
        "h3 a[href]",
        "h4 a[href]",
    )

    fun extract(doc: Document): List<AggregatorArticle> {
        val baseUri = doc.baseUri().ifBlank { "https://localhost" }
        val seen = linkedMapOf<String, String>()

        for (selector in StructuredSelectors) {
            doc.select(selector).forEach { anchor ->
                considerAnchor(seen, anchor, baseUri)
            }
            if (seen.size >= 12) break
        }

        if (seen.size < 6) {
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
        if (!isPlausibleHeadlineText(title)) return
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

    internal fun isPlausibleHeadlineText(text: String): Boolean {
        if (text.length < 12 || text.length > 320) return false
        val lower = text.lowercase()
        val banned = setOf(
            "watch", "listen", "sign in", "log in", "subscribe", "cookies", "ad choices",
            "follow us", "terms of use", "privacy policy",
        )
        if (lower in banned) return false
        if (text.all { it.isDigit() || it.isWhitespace() || it == ':' || it == '|' }) return false
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

        val path = parsed.path.trim('/')
        if (path.length < 4) return false

        return true
    }
}
