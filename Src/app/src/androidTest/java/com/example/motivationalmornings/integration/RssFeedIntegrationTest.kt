package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.AppDatabase
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
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RssFeedIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: RssFeedViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Fake repository to avoid real network calls in integration tests
    private val fakeRssRepository = object : RssRepository() {
        override fun getRssItems(feedUrl: String): List<RssItem> {
            return when (feedUrl) {
                "https://test.com/rss" ->
                    listOf(RssItem(1, "Test Title", "Test Description", "https://test.com/item/1"))
                "https://feed-a.com/rss" ->
                    listOf(RssItem(1, "Feed A Headline", "Desc A", "https://a.com/1"))
                "https://feed-b.com/rss" ->
                    listOf(RssItem(2, "Feed B Headline", "Desc B", "https://b.com/1"))
                else -> emptyList()
            }
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        viewModel = RssFeedViewModel(
            repository = fakeRssRepository,
            dailyContentDao = db.dailyContentDao(),
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    /** Activity 2 manual T3.1 — Single feed: subscribe and load items (titles/links via fake RSS). */
    @Test
    fun subscribeToFeed_persistsUrlAndLoadsItems() = runTest(testDispatcher) {
        val testUrl = "https://test.com/rss"
        viewModel.onFeedUrlChanged(testUrl)
        viewModel.subscribeToFeed()
        
        advanceUntilIdle()

        // Verify it's in the subscribed list
        val subscribed = viewModel.subscribedFeeds.first { it.contains(testUrl) }
        assertTrue("URL should be in subscribed feeds", subscribed.contains(testUrl))

        // Verify items are loaded
        val items = viewModel.rssItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertEquals("Test Title", items[0].title)

        // Verify it's actually in the database
        val dbUrls = db.dailyContentDao().getRssFeedUrls().first()
        assertTrue("URL should be persisted in DB", dbUrls.contains(testUrl))
    }

    @Test
    fun unsubscribeFromFeed_removesFromDbAndList() = runTest(testDispatcher) {
        val testUrl = "https://test.com/rss"
        
        // Setup: subscribe first
        viewModel.onFeedUrlChanged(testUrl)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        
        // Act: unsubscribe
        viewModel.unsubscribeFromFeed(testUrl)
        advanceUntilIdle()

        // Verify it's removed from ViewModel state
        val subscribed = viewModel.subscribedFeeds.first { !it.contains(testUrl) }
        assertTrue("URL should be removed from subscribed feeds", !subscribed.contains(testUrl))

        // Verify it's removed from database
        val dbUrls = db.dailyContentDao().getRssFeedUrls().first()
        assertTrue("URL should be removed from DB", !dbUrls.contains(testUrl))
    }

    @Test
    fun loadFeed_updatesRssItems() = runTest(testDispatcher) {
        val testUrl = "https://test.com/rss"
        viewModel.loadFeed(testUrl)
        advanceUntilIdle()

        val items = viewModel.rssItems.first { it.isNotEmpty() }
        assertEquals(1, items.size)
        assertEquals("Test Title", items[0].title)
    }

    /** Activity 2 manual T3.2 — Two feeds; loading each shows that feed’s items. */
    @Test
    fun t3_2_multipleFeeds_switchingLoadFeeds_updatesItems() = runTest(testDispatcher) {
        val urlA = "https://feed-a.com/rss"
        val urlB = "https://feed-b.com/rss"
        viewModel.onFeedUrlChanged(urlA)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        assertEquals("Feed A Headline", viewModel.rssItems.value[0].title)

        viewModel.loadFeed(urlB)
        advanceUntilIdle()
        assertEquals("Feed B Headline", viewModel.rssItems.value[0].title)
    }

    /** Activity 2 manual T3.3 — No subscriptions: subscribed list empty (empty-state copy is UI-tested). */
    @Test
    fun t3_3_noSubscriptions_subscribedFeedsEmpty() = runTest(testDispatcher) {
        val url = "https://test.com/rss"
        viewModel.onFeedUrlChanged(url)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.unsubscribeFromFeed(url)
        advanceUntilIdle()
        val feeds = viewModel.subscribedFeeds.first { it.isEmpty() }
        assertTrue(feeds.isEmpty())
    }

    /** Activity 2 manual T3.4 — Unknown URL yields empty items (no crash). */
    @Test
    fun t3_4_invalidOrUnknownUrl_returnsEmptyItems() = runTest(testDispatcher) {
        val badUrl = "https://invalid-feed.example/notfound.xml"
        viewModel.onFeedUrlChanged(badUrl)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        assertTrue(viewModel.rssItems.value.isEmpty())
        assertTrue(viewModel.subscribedFeeds.value.contains(badUrl))
    }

    /** Activity 2 manual T4.1 — Subscribed URLs in UI state match `rss_feed_urls` rows. */
    @Test
    fun t4_1_twoSubscribedFeeds_matchDatabaseRows() = runTest(testDispatcher) {
        val u1 = "https://feed-a.com/rss"
        val u2 = "https://feed-b.com/rss"
        viewModel.onFeedUrlChanged(u1)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.onFeedUrlChanged(u2)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        val fromDb = db.dailyContentDao().getRssFeedUrls().first()
        assertEquals(2, fromDb.size)
        assertEquals(setOf(u1, u2), viewModel.subscribedFeeds.value.toSet())
    }

    /** Activity 2 manual T4.2 — Unsubscribe removes URL from state and DB. */
    @Test
    fun t4_2_unsubscribe_removesChipAndDatabaseRow() = runTest(testDispatcher) {
        val u1 = "https://feed-a.com/rss"
        val u2 = "https://feed-b.com/rss"
        viewModel.onFeedUrlChanged(u1)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.onFeedUrlChanged(u2)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.unsubscribeFromFeed(u1)
        advanceUntilIdle()
        val fromDb = db.dailyContentDao().getRssFeedUrls().first()
        assertEquals(1, fromDb.size)
        assertTrue(fromDb.contains(u2))
        assertTrue(u1 !in viewModel.subscribedFeeds.value)
    }

    /** Activity 2 manual T4.3 — No feeds: DB has zero RSS URLs. */
    @Test
    fun t4_3_unsubscribeAll_databaseHasZeroRssUrls() = runTest(testDispatcher) {
        val url = "https://test.com/rss"
        viewModel.onFeedUrlChanged(url)
        viewModel.subscribeToFeed()
        advanceUntilIdle()
        viewModel.unsubscribeFromFeed(url)
        advanceUntilIdle()
        assertTrue(db.dailyContentDao().getRssFeedUrls().first().isEmpty())
        assertTrue(viewModel.subscribedFeeds.value.isEmpty())
    }
}
