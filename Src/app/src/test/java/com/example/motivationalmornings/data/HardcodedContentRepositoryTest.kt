package com.example.motivationalmornings.data

import com.example.motivationalmornings.Persistence.HardcodedContentRepository
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HardcodedContentRepositoryTest {
    @Test
    fun getQuote_returnsQuoteBasedOnDay() = runTest {
        val repo = HardcodedContentRepository()
        val allQuotes = repo.getAllQuotes().first().sortedBy { it.uid }
        val dayIndex = (LocalDate.now().toEpochDay() % allQuotes.size).toInt()
        val expectedQuote = allQuotes[dayIndex].text

        assertEquals(expectedQuote, repo.getQuote().first())
    }

    @Test
    fun getImageOfTheDay_returnsOneOfDayImages() = runTest {
        val repo = HardcodedContentRepository()
        val validIds = listOf(
            R.drawable.imageotd, R.drawable.imageotd2, R.drawable.imageotd3,
            R.drawable.imageotd4, R.drawable.imageotd5, R.drawable.imageotd6
        )
        val image = repo.getImageOfTheDay().first()
        assertNotNull(image)
        assertTrue(image?.drawableResId in validIds)
    }

    @Test
    fun saveIntention_updatesIntentionsFlow() = runTest {
        val repo = HardcodedContentRepository()

        repo.saveIntention("Finish my tasks calmly.")

        val intentions = repo.getIntentions().first()
        assertEquals(1, intentions.size)
        assertEquals("Finish my tasks calmly.", intentions[0])
    }

    @Test
    fun getAllQuotes_initialDefaultContent() = runTest {
        val repo = HardcodedContentRepository()
        val quotes = repo.getAllQuotes().first()
        assertEquals(5, quotes.size)
        assertTrue(quotes.any { it.text == "The best way to predict the future is to create it." })
    }

    @Test
    fun saveQuote_addsToAllQuotes() = runTest {
        val repo = HardcodedContentRepository()
        val initialCount = repo.getAllQuotes().first().size
        repo.saveQuote("New quote text")
        val allQuotes = repo.getAllQuotes().first()
        assertEquals(initialCount + 1, allQuotes.size)
        assertTrue(allQuotes.any { it.text == "New quote text" })
    }

    @Test
    fun deleteQuote_removesFromList() = runTest {
        val repo = HardcodedContentRepository()
        repo.saveQuote("First")
        repo.saveQuote("Second")
        val quotes = repo.getAllQuotes().first()
        val countBeforeDelete = quotes.size
        val quoteToDelete = quotes[0]
        repo.deleteQuote(quoteToDelete)
        val afterDelete = repo.getAllQuotes().first()
        assertEquals(countBeforeDelete - 1, afterDelete.size)
        assertTrue(afterDelete.none { it.uid == quoteToDelete.uid })
    }
}
