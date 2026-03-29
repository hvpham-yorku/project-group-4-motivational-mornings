package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.FakeAnalyticsRepository
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Persistence.RoomContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

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
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Populate with some default data
        defaultQuotes.forEach { text ->
            db.dailyContentDao().insertQuote(QuoteOfTheDay(text = text))
        }

        val repository = RoomContentRepository(db.dailyContentDao())
        val analytics = Analytics(FakeAnalyticsRepository())
        
        viewModel = DailyContentViewModel(repository, analytics, context)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun viewModel_initialization_loadsQuoteFromDb() = runTest(testDispatcher) {
        // We need to trigger the lazy StateFlow by accessing it or subscribing
        val quote = viewModel.quote.first { it != "Loading quote..." }
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
}
