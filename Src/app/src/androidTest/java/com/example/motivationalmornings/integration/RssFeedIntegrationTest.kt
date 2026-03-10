package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.BusinessLogic.RssFeedViewModel
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.RssItem
import com.example.motivationalmornings.Persistence.RssRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
            return if (feedUrl == "https://test.com/rss") {
                listOf(RssItem(1, "Test Title", "Test Description", "https://test.com/item/1"))
            } else {
                emptyList()
            }
        }
    }

    @Before
    fun setup() {
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
    }

    @Test
    fun subscribeToFeed_persistsUrlAndLoadsItems() = runTest(testDispatcher) {
        val testUrl = "https://test.com/rss"
        viewModel.onFeedUrlChanged(testUrl)
        viewModel.subscribeToFeed()
        
        advanceUntilIdle()

        // Verify it's in the subscribed list
        val subscribed = viewModel.subscribedFeeds.value
        assertTrue("URL should be in subscribed feeds", subscribed.contains(testUrl))

        // Verify items are loaded
        val items = viewModel.rssItems.value
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
        val subscribed = viewModel.subscribedFeeds.value
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

        val items = viewModel.rssItems.value
        assertEquals(1, items.size)
        assertEquals("Test Title", items[0].title)
    }
}
