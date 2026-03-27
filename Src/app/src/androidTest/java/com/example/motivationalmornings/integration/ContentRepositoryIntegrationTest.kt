package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Persistence.RoomContentRepository
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
        
        // Ensure the DB starts clean but with our expected defaults
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
        // Wait for a non-null/non-empty quote if necessary
        val quote = repository.getQuote().first { it.isNotBlank() }
        assertTrue("Quote '$quote' should be in defaults", quote in defaultQuotes)
    }

    @Test
    fun getAllQuotes_returnsDefaultContent() = runTest {
        val quotes = repository.getAllQuotes().first { it.isNotEmpty() }
        assertEquals(defaultQuotes.size, quotes.size)
        val texts = quotes.map { it.text }
        defaultQuotes.forEach { assertTrue("Expected quote '$it' not found", it in texts) }
    }

    @Test
    fun saveIntention_andGetIntentions_persists() = runTest {
        val intentionText = "My intention for today"
        repository.saveIntention(intentionText)
        
        // Check that it's actually in the flow
        val intentions = repository.getIntentions().first { it.contains(intentionText) }
        assertTrue(intentions.contains(intentionText))
    }

    @Test
    fun saveQuote_addsToDatabase() = runTest {
        val newQuote = "New saved quote"
        repository.saveQuote(newQuote)
        
        val quotes = repository.getAllQuotes().first { list -> list.any { it.text == newQuote } }
        assertTrue(quotes.any { it.text == newQuote })
    }

    @Test
    fun deleteQuote_removesFromDatabase() = runTest {
        val quotesBefore = repository.getAllQuotes().first { it.isNotEmpty() }
        val toDelete = quotesBefore.first()
        
        repository.deleteQuote(toDelete)
        
        val quotesAfter = repository.getAllQuotes().first { list -> list.none { it.uid == toDelete.uid } }
        assertEquals(quotesBefore.size - 1, quotesAfter.size)
        assertTrue(quotesAfter.none { it.uid == toDelete.uid })
    }
}
