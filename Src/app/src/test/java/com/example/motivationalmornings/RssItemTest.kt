package com.example.motivationalmornings

import com.example.motivationalmornings.Persistence.RssItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssItemTest {

    @Test
    fun rssItem_createsWithAllFields() {
        val item = RssItem(
            id = 1,
            title = "Test Title",
            description = "Test Description",
            link = "https://test.com"
        )

        assertEquals(1, item.id)
        assertEquals("Test Title", item.title)
        assertEquals("Test Description", item.description)
        assertEquals("https://test.com", item.link)
    }

    @Test
    fun rssItem_supportsEmptyDescription() {
        val item = RssItem(
            id = 1,
            title = "Test",
            description = "",
            link = "https://test.com"
        )

        assertEquals("", item.description)
    }

    @Test
    fun rssItem_equality() {
        val item1 = RssItem(1, "Title", "Desc", "Link")
        val item2 = RssItem(1, "Title", "Desc", "Link")

        assertEquals(item1, item2)
    }

    @Test
    fun rssItem_inequality_differentId() {
        val item1 = RssItem(1, "Title", "Desc", "Link")
        val item2 = RssItem(2, "Title", "Desc", "Link")

        assertTrue(item1 != item2)
    }

    @Test
    fun rssItem_copy_works() {
        val original = RssItem(1, "Title", "Desc", "Link")
        val copied = original.copy(title = "New Title")

        assertEquals(1, copied.id)
        assertEquals("New Title", copied.title)
        assertEquals("Desc", copied.description)
        assertEquals("Link", copied.link)
    }
}
