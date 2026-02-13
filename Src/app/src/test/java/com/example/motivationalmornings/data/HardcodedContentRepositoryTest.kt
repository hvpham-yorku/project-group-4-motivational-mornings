package com.example.motivationalmornings.data

import com.example.motivationalmornings.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun getImageResId_returnsLauncherBackground() = runTest {
        val repo = HardcodedContentRepository()

        assertEquals(R.drawable.ic_launcher_background, repo.getImageResId().first())
    }

    @Test
    fun saveIntention_updatesIntentionsFlow() = runTest {
        val repo = HardcodedContentRepository()

        repo.saveIntention("Finish my tasks calmly.")

        assertEquals("Finish my tasks calmly.", repo.getIntentions().first())
    }
}

