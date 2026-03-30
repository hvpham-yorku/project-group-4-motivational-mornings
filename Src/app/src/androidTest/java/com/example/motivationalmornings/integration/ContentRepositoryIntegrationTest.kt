package com.example.motivationalmornings.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motivationalmornings.Persistence.AppDatabase
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.Persistence.RoomContentRepository
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        db.dailyContentDao().insertImage(ImageOfTheDay(uid = 1, drawableResId = com.example.motivationalmornings.R.drawable.imageotd))
        
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

    /** Activity 2 manual T1.1 — Non-empty pool yields an image (no crash). */
    @Test
    fun getImageOfTheDay_returnsPopulatedImage() = runTest {
        val image = repository.getImageOfTheDay().first()
        assertNotNull(image)
        assertEquals(R.drawable.imageotd, image?.drawableResId)
    }

    /** Activity 2 manual T1.1 — empty pool: no crash at repository level (null image). */
    @Test
    fun t1_1_emptyImagePool_returnsNull() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val emptyImagesDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        defaultQuotes.forEach { text ->
            emptyImagesDb.dailyContentDao().insertQuote(QuoteOfTheDay(text = text))
        }
        val repo = RoomContentRepository(emptyImagesDb.dailyContentDao())
        assertNull(repo.getImageOfTheDay().first())
        emptyImagesDb.close()
    }

    /** Activity 2 manual T1.2 — deterministic index: epoch day modulo pool size. */
    @Test
    fun t1_2_imageOfTheDay_matchesEpochDayModuloPoolSize() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val testDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        defaultQuotes.forEach { text ->
            testDb.dailyContentDao().insertQuote(QuoteOfTheDay(text = text))
        }
        val drawableIds = listOf(R.drawable.imageotd, R.drawable.imageotd2, R.drawable.imageotd3)
        drawableIds.forEachIndexed { i, resId ->
            testDb.dailyContentDao().insertImage(ImageOfTheDay(uid = i + 1, drawableResId = resId))
        }
        val repo = RoomContentRepository(testDb.dailyContentDao())
        val sorted = repo.getAllImages().first { it.size == 3 }.sortedBy { it.uid }
        val expectedIndex = (LocalDate.now().toEpochDay() % sorted.size).toInt()
        val expected = sorted[expectedIndex]
        val actual = repo.getImageOfTheDay().first()
        assertEquals(expected.uid, actual?.uid)
        assertEquals(expected.drawableResId, actual?.drawableResId)
        testDb.close()
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
