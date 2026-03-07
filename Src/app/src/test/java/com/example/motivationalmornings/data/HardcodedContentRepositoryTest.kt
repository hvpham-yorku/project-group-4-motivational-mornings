package com.example.motivationalmornings.data

import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedContentRepositoryTest {
    @Test
    fun getQuote_returnsHardcodedQuote() = runTest {
        val repo = HardcodedContentRepository()

        assertEquals(
            "The best way to predict the future is to create it.",
            repo.getQuote().first()
        )
    }

    @Test
    fun getImageResId_returnsOneOfDayImages() = runTest {
        val repo = HardcodedContentRepository()
        val validIds = listOf(
            R.drawable.imageotd, R.drawable.imageotd2, R.drawable.imageotd3,
            R.drawable.imageotd4, R.drawable.imageotd5, R.drawable.imageotd6
        )
        assertTrue(repo.getImageResId().first() in validIds)
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
    fun getAllQuotes_initialEmpty() = runTest {
        val repo = HardcodedContentRepository()
        assertTrue(repo.getAllQuotes().first().isEmpty())
    }

    @Test
    fun saveQuote_updatesQuoteAndAddsToAllQuotes() = runTest {
        val repo = HardcodedContentRepository()
        repo.saveQuote("New quote text")
        assertEquals("New quote text", repo.getQuote().first())
        val allQuotes = repo.getAllQuotes().first()
        assertEquals(1, allQuotes.size)
        assertEquals("New quote text", allQuotes[0].text)
    }

    @Test
    fun deleteQuote_removesFromList() = runTest {
        val repo = HardcodedContentRepository()
        repo.saveQuote("First")
        repo.saveQuote("Second")
        val quotes = repo.getAllQuotes().first()
        assertEquals(2, quotes.size)
        repo.deleteQuote(quotes[1])
        val afterDelete = repo.getAllQuotes().first()
        assertEquals(1, afterDelete.size)
        assertEquals("Second", afterDelete[0].text)
    }
}

