package com.example.motivationalmornings.data

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsHeadlineExtractorTest {

    @Test
    fun extract_prefersArticleAndHeadingLinks() {
        val html = """
            <html><body>
            <article><a href="/2024/06/09/world/story-one">World leaders meet for climate summit talks</a></article>
            <h2><a href="https://news.example.com/story-two">Regional markets react to policy shift today</a></h2>
            <a href="/short">Nope</a>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html, "https://www.example.com/world")
        val headlines = NewsHeadlineExtractor.extract(doc)
        assertEquals(2, headlines.size)
        assertTrue(headlines.any { it.title.contains("climate summit", ignoreCase = true) })
        assertTrue(headlines.any { it.url.contains("story-two") })
    }

    @Test
    fun extract_filtersNoiseLinks() {
        val html = """
            <html><body>
            <a href="mailto:editors@example.com">Contact us with your story tips</a>
            <a href="https://example.com/2024/01/a/article">Valid headline text for testing scrape logic</a>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html, "https://example.com/")
        val headlines = NewsHeadlineExtractor.extract(doc)
        assertEquals(1, headlines.size)
        assertTrue(headlines[0].url.startsWith("https://"))
    }
}
