package com.example.motivationalmornings.data

import com.example.motivationalmornings.Persistence.NewsHeadlineExtractor
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
            <h2><a href="https://news.example.com/2024/06/10/world/regional-markets-policy-shift">Regional markets react to policy shift today across the region</a></h2>
            <a href="/short">This text is too short anyway</a>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html, "https://www.example.com/world")
        val headlines = NewsHeadlineExtractor.extract(doc)
        assertEquals(2, headlines.size)
        assertTrue(headlines.any { it.title.contains("climate summit", ignoreCase = true) })
        assertTrue(headlines.any { it.url.contains("regional-markets") })
    }

    @Test
    fun extract_filtersNoiseLinks() {
        val html = """
            <html><body>
            <a href="mailto:editors@example.com">Contact us with your story tips here today</a>
            <a href="https://example.com/2024/01/15/world/article-slug-for-testing-headlines">Valid headline text for testing scrape logic here today</a>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html, "https://example.com/")
        val headlines = NewsHeadlineExtractor.extract(doc)
        assertEquals(1, headlines.size)
        assertTrue(headlines[0].url.startsWith("https://"))
    }

    @Test
    fun extract_excludesCategoryNavigationLinks() {
        val html = """
            <html><body>
            <nav>
              <a href="https://news.example.com/world">World</a>
              <a href="https://news.example.com/world/asia">Asia Pacific</a>
              <a href="https://news.example.com/politics">Politics</a>
            </nav>
            <article>
              <a href="https://news.example.com/2024/03/15/world/earthquake-readiness-planning">
                Officials outline earthquake readiness steps for coastal cities today
              </a>
            </article>
            </body></html>
        """.trimIndent()
        val doc = Jsoup.parse(html, "https://news.example.com/world")
        val headlines = NewsHeadlineExtractor.extract(doc)
        assertEquals(1, headlines.size)
        assertTrue(headlines[0].url.contains("/2024/03/15/"))
        assertTrue(headlines[0].title.contains("earthquake", ignoreCase = true))
    }
}
