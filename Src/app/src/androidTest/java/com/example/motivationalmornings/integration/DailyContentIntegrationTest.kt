package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.ContentReactions
import com.example.motivationalmornings.Persistence.FakeAnalyticsRepository
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Persistence.RoomContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DailyContentIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: DailyContentViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val defaultQuotes = listOf(
        "The best way to predict the future is to create it.",
        "Every morning is a new opportunity to become a better version of yourself.",
        "Small steps every day lead to big changes.",
        "You are capable of amazing things today.",
        "Start where you are. Use what you have. Do what you can."
    )

    @Before
    fun setup() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        
        // Configure Room to use the test dispatcher so advanceUntilIdle() waits for DB operations
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(testDispatcher.asExecutor())
            .setTransactionExecutor(testDispatcher.asExecutor())
            .build()
        
        // Populate with some default data
        defaultQuotes.forEach { text ->
            db.dailyContentDao().insertQuote(QuoteOfTheDay(text = text))
        }
        db.dailyContentDao().insertImage(ImageOfTheDay(uid = 1, drawableResId = com.example.motivationalmornings.R.drawable.imageotd))

        val repository = RoomContentRepository(db.dailyContentDao())
        val analytics = Analytics(FakeAnalyticsRepository())
        
        // Pass a no-op refreshWidgets to avoid background work that might outlive the test
        // and cause "connection is closed" errors during tearDown.
        viewModel = DailyContentViewModel(repository, analytics, context, refreshWidgets = {})
        
        advanceUntilIdle()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun viewModel_initialization_loadsQuoteFromDb() = runTest(testDispatcher) {
        // We need to trigger the lazy StateFlow by accessing it or subscribing
        val quote = viewModel.quote.first { it != "Loading quote..." && it.isNotBlank() }
        assertTrue("Quote should be one of the defaults: $quote", quote in defaultQuotes)
    }

    @Test
    fun saveIntention_persistsToDbAndUpdatesState() = runTest(testDispatcher) {
        val intentionText = "Test my integration"
        viewModel.saveIntention(intentionText)
        
        advanceUntilIdle()

        // Verify state update - using first to ensure we collect the latest update
        val intentions = viewModel.intentions.first { it.contains(intentionText) }
        assertTrue("Intention should be in the list", intentions.contains(intentionText))

        // Verify database persistence
        val dbIntentions = db.dailyContentDao().getAllIntentions().first()
        val savedIntention = dbIntentions.find { it.text == intentionText }
        assertNotNull("Intention should be persisted in DB", savedIntention)
        assertNotNull("Saved intention should have a time", savedIntention?.time)
        assertTrue("Time should follow HH:mm format", savedIntention?.time?.matches(Regex("\\d{2}:\\d{2}")) == true)
    }

    @Test
    fun saveQuote_persistsToDbAndUpdatesAllQuotes() = runTest(testDispatcher) {
        val newQuote = "Integration tests are important"
        viewModel.saveQuote(newQuote)
        
        advanceUntilIdle()

        val allQuotes = viewModel.allQuotes.first { quotes -> quotes.any { it.text == newQuote } }
        assertTrue("New quote should be in the list", allQuotes.any { it.text == newQuote })

        val dbQuotes = db.dailyContentDao().getAllQuotes().first()
        assertTrue("New quote should be in the DB", dbQuotes.any { it.text == newQuote })
    }

    @Test
    fun deleteQuote_removesFromDbAndState() = runTest(testDispatcher) {
        // Ensure data is loaded
        val initialQuotes = viewModel.allQuotes.first { it.isNotEmpty() }
        val toDelete = initialQuotes.first()
        
        viewModel.deleteQuote(toDelete)
        advanceUntilIdle()

        val remainingQuotes = viewModel.allQuotes.first { quotes -> quotes.none { it.uid == toDelete.uid } }
        assertTrue("Quote should be removed from state", remainingQuotes.none { it.uid == toDelete.uid })

        val dbQuotes = db.dailyContentDao().getAllQuotes().first()
        assertTrue("Quote should be removed from DB", dbQuotes.none { it.uid == toDelete.uid })
    }

    /** Activity 2 manual T6.1 — Like quote; DB holds LIKE for that quote. */
    @Test
    fun t6_1_likeQuote_persistsSingleLikeRowInDatabase() = runTest(testDispatcher) {
        viewModel.quoteOfTheDay.first { it != null }
        advanceUntilIdle() // Ensure StateFlow.value is updated
        val quote = viewModel.quoteOfTheDay.value
        
        viewModel.likeQuote()
        advanceUntilIdle()
        
        val rows = db.dailyContentDao().getAllQuoteFeedback().first()
        assertEquals(1, rows.size)
        assertEquals(quote!!.uid, rows[0].quoteId)
        assertEquals(ContentReactions.LIKE, rows[0].reaction)
    }

    /** Activity 2 manual T6.2 — Dislike quote. */
    @Test
    fun t6_2_dislikeQuote_persistsDislikeInDatabase() = runTest(testDispatcher) {
        viewModel.quoteOfTheDay.first { it != null }
        advanceUntilIdle() // Ensure StateFlow.value is updated
        
        viewModel.dislikeQuote()
        advanceUntilIdle()
        
        val rows = db.dailyContentDao().getAllQuoteFeedback().first()
        assertEquals(1, rows.size)
        assertEquals(ContentReactions.DISLIKE, rows[0].reaction)
    }

    /** Activity 2 manual T6.3 — Only latest reaction row for that quote. */
    @Test
    fun t6_3_toggleQuoteReaction_keepsOnlyLatestRowInDatabase() = runTest(testDispatcher) {
        viewModel.quoteOfTheDay.first { it != null }
        advanceUntilIdle() // Ensure StateFlow.value is updated
        
        viewModel.likeQuote()
        advanceUntilIdle()
        viewModel.dislikeQuote()
        advanceUntilIdle()
        
        val rows = db.dailyContentDao().getAllQuoteFeedback().first()
        assertEquals(1, rows.size)
        assertEquals(ContentReactions.DISLIKE, rows[0].reaction)
    }

    /** Activity 2 manual T6.4 — Preference survives a new ViewModel (same DB) as after process death. */
    @Test
    fun t6_4_imageLikePersistsForNewViewModelInstance() = runTest(testDispatcher) {
        viewModel.imageOfTheDay.first { it != null }
        advanceUntilIdle() // Ensure StateFlow.value is updated
        val image = viewModel.imageOfTheDay.value
        
        viewModel.likeImage()
        advanceUntilIdle()
        
        val feedbackRows = db.dailyContentDao().getAllImageFeedback().first()
        assertEquals(1, feedbackRows.size)
        assertEquals(image!!.uid, feedbackRows[0].imageId)
        assertEquals(ContentReactions.LIKE, feedbackRows[0].reaction)

        val context: Context = ApplicationProvider.getApplicationContext()
        val repository2 = RoomContentRepository(db.dailyContentDao())
        val vm2 = DailyContentViewModel(repository2, Analytics(FakeAnalyticsRepository()), context, refreshWidgets = {})

        val reaction = vm2.imageReaction.first { it != null }
        assertEquals(ContentReactions.LIKE, reaction)
    }
}
