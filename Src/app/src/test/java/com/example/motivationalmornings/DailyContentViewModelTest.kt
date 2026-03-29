package com.example.motivationalmornings

import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import android.content.Context
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@ExperimentalCoroutinesApi
class DailyContentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockContentRepository
    private lateinit var viewModel: DailyContentViewModel
    private lateinit var mockAnalyticsRepository: MockAnalyticsRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockContentRepository()
        mockAnalyticsRepository = MockAnalyticsRepository()
        val appContext = mock(Context::class.java)
        `when`(appContext.applicationContext).thenReturn(appContext)
        viewModel = DailyContentViewModel(
            mockRepository,
            Analytics(mockAnalyticsRepository),
            appContext,
            refreshWidgets = {},
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun TestScope.triggerStateFlows() {
        viewModel.quote.first()
        viewModel.intentions.first()
        viewModel.allIntentions.first()
        viewModel.allQuotes.first()
        viewModel.imageResId.first()
        advanceUntilIdle()
    }

    @Test
    fun initialQuote_loadsFromRepository() = runTest {
        triggerStateFlows()
        val quote = viewModel.quote.value
        assertEquals("Test quote from repository", quote)
    }

    @Test
    fun initialImageResId_loadsFromRepository() = runTest {
        triggerStateFlows()
        val imageResId = viewModel.imageResId.value
        assertEquals(R.drawable.ic_launcher_background, imageResId)
    }

    @Test
    fun initialIntentions_isEmpty() = runTest {
        triggerStateFlows()
        val intentions = viewModel.intentions.value
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_addsToList() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("Exercise for 30 minutes")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertEquals("Exercise for 30 minutes", intentions[0])
    }

    @Test
    fun saveIntention_savesToRepository() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("Read a book")
        advanceUntilIdle()
        assertEquals(1, mockRepository.savedIntentions.size)
        assertEquals("Read a book", mockRepository.savedIntentions[0])
    }

    @Test
    fun saveIntention_savesWithTime() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("Check time")
        advanceUntilIdle()
        val savedIntention = mockRepository.allIntentionsList[0]
        assertNotNull("Saved intention should have a time", savedIntention.time)
        assertTrue("Time should follow HH:mm format", savedIntention.time!!.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun saveIntention_multipleIntentions_addsToTop() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("First intention")
        advanceUntilIdle()
        viewModel.saveIntention("Second intention")
        advanceUntilIdle()
        viewModel.saveIntention("Third intention")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(3, intentions.size)
        assertEquals("Third intention", intentions[0])
        assertEquals("Second intention", intentions[1])
        assertEquals("First intention", intentions[2])
    }

    @Test
    fun saveIntention_blankIntention_notSaved() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_whitespaceOnly_notSaved() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("   ")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_withLeadingTrailingSpaces_savesTrimmedVersion() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("  Clean the house  ")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertTrue(intentions[0].contains("Clean the house"))
    }

    @Test
    fun saveIntention_longText_savesCorrectly() = runTest {
        triggerStateFlows()
        val longIntention = "Complete the project documentation, review code changes, " +
                "attend team meeting, and prepare presentation for stakeholders"
        viewModel.saveIntention(longIntention)
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertEquals(longIntention, intentions[0])
    }

    @Test
    fun saveIntention_specialCharacters_savesCorrectly() = runTest {
        triggerStateFlows()
        val specialIntention = "Buy groceries: milk, eggs & bread (don't forget!)"
        viewModel.saveIntention(specialIntention)
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertEquals(specialIntention, intentions[0])
    }

    @Test
    fun saveIntention_duplicateIntentions_bothSaved() = runTest {
        triggerStateFlows()
        viewModel.saveIntention("Water the plants")
        advanceUntilIdle()
        viewModel.saveIntention("Water the plants")
        advanceUntilIdle()
        val intentions = viewModel.intentions.value
        assertEquals(2, intentions.size)
        assertEquals("Water the plants", intentions[0])
        assertEquals("Water the plants", intentions[1])
    }

    @Test
    fun intentions_orderMaintainedAfterMultipleSaves() = runTest {
        triggerStateFlows()
        val intentionsList = listOf(
            "Morning meditation",
            "Healthy breakfast",
            "Team standup",
            "Code review",
            "Afternoon walk"
        )
        intentionsList.forEach { intention ->
            viewModel.saveIntention(intention)
            advanceUntilIdle()
        }
        val savedIntentions = viewModel.intentions.value
        assertEquals(5, savedIntentions.size)
        assertEquals("Afternoon walk", savedIntentions[0])
        assertEquals("Morning meditation", savedIntentions[4])
    }

    @Test
    fun saveQuote_addsToAllQuotes() = runTest {
        triggerStateFlows()
        val initialSize = viewModel.allQuotes.value.size
        viewModel.saveQuote("A new motivational quote")
        advanceUntilIdle()
        val quotes = viewModel.allQuotes.value
        assertEquals(initialSize + 1, quotes.size)
        assertEquals("A new motivational quote", quotes[0].text)
    }

    @Test
    fun saveQuote_blankQuote_notSaved() = runTest {
        triggerStateFlows()
        val initialSize = viewModel.allQuotes.value.size
        viewModel.saveQuote("")
        advanceUntilIdle()
        assertEquals(initialSize, viewModel.allQuotes.value.size)
    }

    @Test
    fun saveQuote_whitespaceOnly_notSaved() = runTest {
        triggerStateFlows()
        val initialSize = viewModel.allQuotes.value.size
        viewModel.saveQuote("   ")
        advanceUntilIdle()
        assertEquals(initialSize, viewModel.allQuotes.value.size)
    }

    @Test
    fun deleteQuote_removesFromList() = runTest {
        triggerStateFlows()
        viewModel.saveQuote("Quote to delete")
        advanceUntilIdle()
        val quotesBefore = viewModel.allQuotes.value
        assertTrue(quotesBefore.isNotEmpty())
        val quoteToDelete = quotesBefore[0]
        viewModel.deleteQuote(quoteToDelete)
        advanceUntilIdle()
        val quotesAfter = viewModel.allQuotes.value
        assertEquals(quotesBefore.size - 1, quotesAfter.size)
        assertTrue(quotesAfter.none { it.uid == quoteToDelete.uid })
    }

    // Mock ContentRepository for testing
    private class MockContentRepository : ContentRepository {
        val savedIntentions = mutableListOf<String>()
        val allIntentionsList = mutableListOf<Intention>()
        private val _intentions = MutableStateFlow<List<String>>(emptyList())
        private val _allIntentions = MutableStateFlow<List<Intention>>(emptyList())
        private val _quotes = MutableStateFlow<List<QuoteOfTheDay>>(emptyList())

        override fun getQuote(): Flow<String> = flowOf("Test quote from repository")

        override fun getImageResId(): Flow<Int> = flowOf(R.drawable.ic_launcher_background)

        override fun getIntentions(): Flow<List<String>> = _intentions

        override fun getAllIntentions(): Flow<List<Intention>> = _allIntentions

        override suspend fun saveIntention(intention: String, weather: String?) {
            savedIntentions.add(intention)
            val current = _intentions.value.toMutableList()
            current.add(0, intention)
            _intentions.value = current
            
            val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val newIntention = Intention(
                text = intention, 
                date = LocalDate.now().toString(), 
                weather = weather,
                time = currentTime
            )
            allIntentionsList.add(0, newIntention)
            
            val withMeta = _allIntentions.value.toMutableList()
            withMeta.add(0, newIntention)
            _allIntentions.value = withMeta
        }

        override suspend fun updateReflection(uid: Int, reflection: String) {
            // Not needed for current tests
        }

        override suspend fun saveQuote(quote: String) {
            val currentQuotes = _quotes.value.toMutableList()
            val nextId = (currentQuotes.maxOfOrNull { it.uid } ?: 0) + 1
            currentQuotes.add(0, QuoteOfTheDay(uid = nextId, text = quote))
            _quotes.value = currentQuotes
        }

        override fun getAllQuotes(): Flow<List<QuoteOfTheDay>> = _quotes

        override suspend fun deleteQuote(quote: QuoteOfTheDay) {
            val currentQuotes = _quotes.value.toMutableList()
            currentQuotes.removeAll { it.uid == quote.uid }
            _quotes.value = currentQuotes
        }
    }

    private class MockAnalyticsRepository : AnalyticsRepository {
        override suspend fun trackIntentionSet(event: IntentionAnalyticsEvent) {
            // For now, do nothing
        }
    }
}
