package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.data.RoomContentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * ITR2 integration test: persistence interface tested with actual Room DB (in-memory).
 * Unit tests use the stub (HardcodedContentRepository); this test uses the real database.
 */
@RunWith(AndroidJUnit4::class)
class ContentRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RoomContentRepository

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
        defaultQuotes.forEach { text ->
            db.dailyContentDao().insertQuote(QuoteOfTheDay(text = text))
        }
        repository = RoomContentRepository(db.dailyContentDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getQuote_returnsOneOfDefaultQuotes() = runTest {
        val quote = repository.getQuote().first()
        assertTrue(quote in defaultQuotes)
    }

    @Test
    fun getAllQuotes_returnsDefaultContent() = runTest {
        val quotes = repository.getAllQuotes().first()
        assertEquals(defaultQuotes.size, quotes.size)
        val texts = quotes.map { it.text }
        defaultQuotes.forEach { assertTrue(it in texts) }
    }

    @Test
    fun saveIntention_andGetIntentions_persists() = runTest {
        val today = LocalDate.now().toString()
        repository.saveIntention("My intention for today")
        val intentions = repository.getIntentions().first()
        assertTrue(intentions.contains("My intention for today"))
    }

    @Test
    fun saveQuote_addsToDatabase() = runTest {
        repository.saveQuote("New saved quote")
        val quotes = repository.getAllQuotes().first()
        assertTrue(quotes.any { it.text == "New saved quote" })
    }

    @Test
    fun deleteQuote_removesFromDatabase() = runTest {
        val quotesBefore = repository.getAllQuotes().first()
        val toDelete = quotesBefore.first()
        repository.deleteQuote(toDelete)
        val quotesAfter = repository.getAllQuotes().first()
        assertEquals(quotesBefore.size - 1, quotesAfter.size)
        assertTrue(quotesAfter.none { it.uid == toDelete.uid })
    }
}
