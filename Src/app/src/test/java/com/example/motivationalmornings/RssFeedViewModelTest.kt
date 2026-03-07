package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: RssRepository
    private lateinit var viewModel: RssFeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRssRepository()
        viewModel = RssFeedViewModel(repository, dailyContentDao = null, ioDispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialRssItems_isEmpty() = runTest {
        val items = viewModel.rssItems.first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun initialSubscribedFeeds_isEmpty() = runTest {
        val feeds = viewModel.subscribedFeeds.first()
        assertTrue(feeds.isEmpty())
    }

    @Test
    fun initialCurrentFeedUrl_isEmpty() = runTest {
        assertEquals("", viewModel.currentFeedUrl.value)
    }

    @Test
    fun onFeedUrlChanged_updatesCurrentFeedUrl() = runTest {
        viewModel.onFeedUrlChanged("https://example.com/feed")
        assertEquals("https://example.com/feed", viewModel.currentFeedUrl.value)
    }

    @Test
    fun loadFeed_populatesRssItemsFromRepository() = runTest {
        viewModel.loadFeed("https://example.com/feed")
        advanceUntilIdle()
        val items = viewModel.rssItems.value
        assertEquals(3, items.size)
    }

    @Test
    fun loadFeed_itemsContainExpectedContent() = runTest {
        viewModel.loadFeed("https://example.com/feed")
        advanceUntilIdle()
        val items = viewModel.rssItems.value
        assertEquals("Morning Motivation", items[0].title)
        assertEquals("Start your day with a positive quote.", items[0].description)
        assertEquals("Mindfulness Minute", items[1].title)
        assertEquals("Gratitude Check", items[2].title)
    }

    @Test
    fun loadFeed_itemsAllHaveValidUrls() = runTest {
        viewModel.loadFeed("https://example.com/feed")
        advanceUntilIdle()
        val items = viewModel.rssItems.value
        assertTrue(items.all { it.link.startsWith("https://") })
    }

    @Test
    fun loadFeed_maintainsOrderFromRepository() = runTest {
        viewModel.loadFeed("https://example.com/feed")
        advanceUntilIdle()
        val repoItems = repository.getRssItems("ignored")
        val vmItems = viewModel.rssItems.value
        assertEquals(repoItems[0].id, vmItems[0].id)
        assertEquals(repoItems[1].id, vmItems[1].id)
        assertEquals(repoItems[2].id, vmItems[2].id)
    }

    @Test
    fun subscribeToFeed_addsUrlAndLoadsFeedAndClearsInput() = runTest {
        viewModel.onFeedUrlChanged("https://example.com/feed")
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        assertEquals(listOf("https://example.com/feed"), viewModel.subscribedFeeds.value)
        assertEquals(3, viewModel.rssItems.value.size)
        assertEquals("", viewModel.currentFeedUrl.value)
    }

    @Test
    fun subscribeToFeed_withEmptyUrl_doesNothing() = runTest {
        viewModel.onFeedUrlChanged("")
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        assertTrue(viewModel.subscribedFeeds.value.isEmpty())
    }

    @Test
    fun subscribeToFeed_withDuplicateUrl_doesNotAddAgain() = runTest {
        viewModel.onFeedUrlChanged("https://example.com/feed")
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.onFeedUrlChanged("https://example.com/feed")
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        assertEquals(1, viewModel.subscribedFeeds.value.size)
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
