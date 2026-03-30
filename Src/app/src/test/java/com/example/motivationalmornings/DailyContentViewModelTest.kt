package com.example.motivationalmornings

import android.content.Context
import com.example.motivationalmornings.BusinessLogic.Analytics
import com.example.motivationalmornings.BusinessLogic.DailyContentViewModel
import com.example.motivationalmornings.Persistence.AnalyticsRepository
import com.example.motivationalmornings.Persistence.ContentReactions
import com.example.motivationalmornings.Persistence.ContentRepository
import com.example.motivationalmornings.Persistence.ImageOfTheDay
import com.example.motivationalmornings.Persistence.Intention
import com.example.motivationalmornings.Persistence.IntentionAnalyticsEvent
import com.example.motivationalmornings.Persistence.QuoteOfTheDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.map

data class RecordedReaction(
    val id: Int,
    val reaction: String
)

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
        viewModel.imageOfTheDay.first()
        advanceUntilIdle()
    }

    @Test
    fun initialQuote_loadsFromRepository() = runTest {
        triggerStateFlows()
        val quote = viewModel.quote.value
        assertEquals("Test quote from repository", quote)
    }

    @Test
    fun initialImage_loadsFromRepository() = runTest {
        triggerStateFlows()
        val image = viewModel.imageOfTheDay.value
        assertNotNull(image)
        assertEquals(R.drawable.imageotd, image?.drawableResId)
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

    /** Activity 2 manual T6.1 — Like quote (ViewModel → repository). */
    @Test
    fun t6_1_likeQuote_recordsLikeOnRepository() = runTest {
        triggerStateFlows()
        viewModel.likeQuote()
        advanceUntilIdle()
        assertEquals(ContentReactions.LIKE, mockRepository.lastQuoteReaction)
    }

    /** Activity 2 manual T6.2 — Dislike quote. */
    @Test
    fun t6_2_dislikeQuote_recordsDislikeOnRepository() = runTest {
        triggerStateFlows()
        viewModel.dislikeQuote()
        advanceUntilIdle()
        assertEquals(ContentReactions.DISLIKE, mockRepository.lastQuoteReaction)
    }

    /** Activity 2 manual T6.3 — Latest reaction wins. */
    @Test
    fun t6_3_toggleLikeThenDislike_keepsDislikeOnly() = runTest {
        triggerStateFlows()
        viewModel.likeQuote()
        advanceUntilIdle()
        viewModel.dislikeQuote()
        advanceUntilIdle()
        assertEquals(ContentReactions.DISLIKE, mockRepository.lastQuoteReaction)
    }

    /** Image like path (manual T6.4 also requires DB persistence; see Android integration test). */
    @Test
    fun likeImage_recordsLikeOnRepository() = runTest {
        triggerStateFlows()
        viewModel.likeImage()
        advanceUntilIdle()
        assertEquals(ContentReactions.LIKE, mockRepository.lastImageReaction)
    }

    @Test
    fun dislikeImage_recordsDislikeOnRepository() = runTest {
        triggerStateFlows()
        viewModel.dislikeImage()
        advanceUntilIdle()
        assertEquals(ContentReactions.DISLIKE, mockRepository.lastImageReaction)
    }

    // Mock ContentRepository for testing
    private class MockContentRepository : ContentRepository {
        val savedIntentions = mutableListOf<String>()
        val allIntentionsList = mutableListOf<Intention>()
        private val _intentions = MutableStateFlow<List<String>>(emptyList())
        private val _allIntentions = MutableStateFlow<List<Intention>>(emptyList())
        private val _quotes = MutableStateFlow<List<QuoteOfTheDay>>(emptyList())
        private val _images = MutableStateFlow<List<ImageOfTheDay>>(listOf(ImageOfTheDay(uid = 1, drawableResId = R.drawable.imageotd)))

        override fun getQuote(): Flow<String> = flowOf("Test quote from repository")
        
        override fun getQuoteOfTheDay(): Flow<QuoteOfTheDay?> = flowOf(QuoteOfTheDay(uid = 1, text = "Test quote from repository"))

        override fun getImageOfTheDay(): Flow<ImageOfTheDay?> = flowOf(_images.value.first())

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

        override fun getAllImages(): Flow<List<ImageOfTheDay>> = _images

        override suspend fun addImage(image: ImageOfTheDay) {
            val current = _images.value.toMutableList()
            current.add(image)
            _images.value = current
        }

        override suspend fun deleteImage(image: ImageOfTheDay) {
            val current = _images.value.toMutableList()
            current.remove(image)
            _images.value = current
        }

        private val _quoteReaction = MutableStateFlow<String?>(null)
        private val _imageReaction = MutableStateFlow<String?>(null)

        /** Latest reaction passed to [recordQuoteReaction] (mirrors DB in integration tests). */
        val lastQuoteReaction: String? get() = _quoteReaction.value

        /** Latest reaction passed to [recordImageReaction]. */
        val lastImageReaction: String? get() = _imageReaction.value

        override suspend fun recordQuoteReaction(quoteId: Int, reaction: String) {
            _quoteReaction.value = reaction
        }

        override suspend fun recordImageReaction(imageId: Int, reaction: String) {
            _imageReaction.value = reaction
        }

        override fun observeQuoteReaction(): Flow<String?> = _quoteReaction.asStateFlow()

        override fun observeImageReaction(): Flow<String?> = _imageReaction.asStateFlow()
    }

    private class MockAnalyticsRepository : AnalyticsRepository {
        override suspend fun trackIntentionSet(event: IntentionAnalyticsEvent) {
        }
    }
}
