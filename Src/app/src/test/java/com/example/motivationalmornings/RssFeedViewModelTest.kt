package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class RssRepositoryTest {

    private lateinit var repository: RssRepository

    @Before
    fun setup() {
        repository = RssRepository()
    }

    @Test
    fun getRssItems_blankUrl_returnsEmptyList() {
        val items = repository.getRssItems(" ")
        assertTrue(items.isEmpty())
    }

    @Test
    fun getRssItems_invalidUrl_returnsEmptyList() {
        val items = repository.getRssItems("not a url")
        assertTrue(items.isEmpty())
    }
}

private class FakeRssRepository : RssRepository() {
    private val fakeItems = listOf(
        RssItem(1, "Morning Motivation", "Start your day with a positive quote.", "https://example.com/morning-motivation"),
        RssItem(2, "Mindfulness Minute", "A short mindfulness exercise for your commute.", "https://example.com/mindfulness-minute"),
        RssItem(3, "Gratitude Check", "Three things to be grateful for today.", "https://example.com/gratitude-check")
    )

    override fun getRssItems(feedUrl: String): List<RssItem> = fakeItems
}

@ExperimentalCoroutinesApi
class RssFeedViewModelTest {

    private lateinit var repository: RssRepository
    private lateinit var viewModel: RssFeedViewModel

    @Before
    fun setup() {
        repository = FakeRssRepository()
        viewModel = RssFeedViewModel(repository)
    }

    @Test
    fun initialRssItems_loadsFromRepository() = runTest {
        val items = viewModel.rssItems.first()
        assertEquals(3, items.size)
    }

    @Test
    fun rssItems_containsExpectedFirstItem() = runTest {
        val items = viewModel.rssItems.value
        val firstItem = items[0]
        assertEquals("Morning Motivation", firstItem.title)
        assertEquals("Start your day with a positive quote.", firstItem.description)
    }

    @Test
    fun rssItems_containsExpectedSecondItem() = runTest {
        val items = viewModel.rssItems.value
        val secondItem = items[1]
        assertEquals("Mindfulness Minute", secondItem.title)
        assertEquals("A short mindfulness exercise for your commute.", secondItem.description)
    }

    @Test
    fun rssItems_containsExpectedThirdItem() = runTest {
        val items = viewModel.rssItems.value
        val thirdItem = items[2]
        assertEquals("Gratitude Check", thirdItem.title)
        assertEquals("Three things to be grateful for today.", thirdItem.description)
    }

    @Test
    fun rssItems_allHaveValidUrls() = runTest {
        val items = viewModel.rssItems.value
        assertTrue(items.all { it.link.startsWith("https://") })
    }

    @Test
    fun rssItems_maintainOrderFromRepository() = runTest {
        val repoItems = repository.getRssItems("ignored")
        val vmItems = viewModel.rssItems.value

        assertEquals(repoItems[0].id, vmItems[0].id)
        assertEquals(repoItems[1].id, vmItems[1].id)
        assertEquals(repoItems[2].id, vmItems[2].id)
    }
}

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
