package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.analytics.Analytics
import com.example.motivationalmornings.data.FakeAnalyticsRepository
import com.example.motivationalmornings.data.RoomContentRepository
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
        
        viewModel = DailyContentViewModel(repository, analytics)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun viewModel_initialization_loadsQuoteFromDb() = runTest(testDispatcher) {
        advanceUntilIdle()
        val quote = viewModel.quote.value
        assertTrue("Quote should be one of the defaults", quote in defaultQuotes)
    }

    @Test
    fun saveIntention_persistsToDbAndUpdatesState() = runTest(testDispatcher) {
        val intentionText = "Test my integration"
        viewModel.saveIntention(intentionText)
        
        advanceUntilIdle()

        // Verify state update
        val intentions = viewModel.intentions.value
        assertTrue("Intention should be in the list", intentions.contains(intentionText))

        // Verify database persistence
        val dbIntentions = db.dailyContentDao().getIntentionsByDate(LocalDate.now().toString()).first()
        assertTrue("Intention should be persisted in DB", dbIntentions.contains(intentionText))
    }

    @Test
    fun saveQuote_persistsToDbAndUpdatesAllQuotes() = runTest(testDispatcher) {
        val newQuote = "Integration tests are important"
        viewModel.saveQuote(newQuote)
        
        advanceUntilIdle()

        val allQuotes = viewModel.allQuotes.value
        assertTrue("New quote should be in the list", allQuotes.any { it.text == newQuote })

        val dbQuotes = db.dailyContentDao().getAllQuotes().first()
        assertTrue("New quote should be in the DB", dbQuotes.any { it.text == newQuote })
    }

    @Test
    fun deleteQuote_removesFromDbAndState() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialQuotes = viewModel.allQuotes.value
        val toDelete = initialQuotes.first()
        
        viewModel.deleteQuote(toDelete)
        advanceUntilIdle()

        val remainingQuotes = viewModel.allQuotes.value
        assertTrue("Quote should be removed from state", remainingQuotes.none { it.uid == toDelete.uid })

        val dbQuotes = db.dailyContentDao().getAllQuotes().first()
        assertTrue("Quote should be removed from DB", dbQuotes.none { it.uid == toDelete.uid })
    }
}
