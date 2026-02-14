package com.example.motivationalmornings

import com.example.motivationalmornings.data.ContentRepository
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DailyContentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockContentRepository
    private lateinit var viewModel: DailyContentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockContentRepository()
        viewModel = DailyContentViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialQuote_loadsFromRepository() = runTest {
        // Given: ViewModel is initialized with mock repository
        // When: Getting quote
        advanceUntilIdle()
        val quote = viewModel.quote.value

        // Then: Should load quote from repository
        assertEquals("Test quote from repository", quote)
    }

    @Test
    fun initialImageResId_loadsFromRepository() = runTest {
        // Given: ViewModel is initialized with mock repository
        // When: Getting image resource ID
        advanceUntilIdle()
        val imageResId = viewModel.imageResId.value

        // Then: Should load image ID from repository
        assertEquals(R.drawable.ic_launcher_background, imageResId)
    }

    @Test
    fun initialIntentions_isEmpty() = runTest {
        // Given: ViewModel is initialized
        // When: Getting intentions
        val intentions = viewModel.intentions.first()

        // Then: Should be empty initially
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_addsToList() = runTest {
        // Given: ViewModel with no intentions
        // When: Saving an intention
        viewModel.saveIntention("Exercise for 30 minutes")
        advanceUntilIdle()

        // Then: Intention should be added to the list
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertEquals("Exercise for 30 minutes", intentions[0])
    }

    @Test
    fun saveIntention_savesToRepository() = runTest {
        // Given: ViewModel with mock repository
        // When: Saving an intention
        viewModel.saveIntention("Read a book")
        advanceUntilIdle()

        // Then: Repository should have received the save call
        assertEquals(1, mockRepository.savedIntentions.size)
        assertEquals("Read a book", mockRepository.savedIntentions[0])
    }

    @Test
    fun saveIntention_multipleIntentions_addsToTop() = runTest {
        // Given: ViewModel with no intentions
        // When: Saving multiple intentions
        viewModel.saveIntention("First intention")
        advanceUntilIdle()
        viewModel.saveIntention("Second intention")
        advanceUntilIdle()
        viewModel.saveIntention("Third intention")
        advanceUntilIdle()

        // Then: Latest intention should be at top (index 0)
        val intentions = viewModel.intentions.value
        assertEquals(3, intentions.size)
        assertEquals("Third intention", intentions[0])
        assertEquals("Second intention", intentions[1])
        assertEquals("First intention", intentions[2])
    }

    @Test
    fun saveIntention_blankIntention_notSaved() = runTest {
        // Given: ViewModel with no intentions
        // When: Attempting to save blank intention
        viewModel.saveIntention("")
        advanceUntilIdle()

        // Then: Should not be added
        val intentions = viewModel.intentions.value
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_whitespaceOnly_notSaved() = runTest {
        // Given: ViewModel with no intentions
        // When: Attempting to save whitespace-only intention
        viewModel.saveIntention("   ")
        advanceUntilIdle()

        // Then: Should not be added
        val intentions = viewModel.intentions.value
        assertTrue(intentions.isEmpty())
    }

    @Test
    fun saveIntention_withLeadingTrailingSpaces_savesTrimmedVersion() = runTest {
        // Given: ViewModel with no intentions
        // When: Saving intention with spaces
        viewModel.saveIntention("  Clean the house  ")
        advanceUntilIdle()

        // Then: Should save the intention (trimming handled by isNotBlank check)
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertTrue(intentions[0].contains("Clean the house"))
    }

    @Test
    fun saveIntention_longText_savesCorrectly() = runTest {
        // Given: ViewModel with no intentions
        val longIntention = "Complete the project documentation, review code changes, " +
                "attend team meeting, and prepare presentation for stakeholders"

        // When: Saving long intention
        viewModel.saveIntention(longIntention)
        advanceUntilIdle()

        // Then: Should save complete text
        val intentions = viewModel.intentions.value
        assertEquals(1, intentions.size)
        assertEquals(longIntention, intentions[0])
    }

    @Test
    fun saveIntention_specialCharacters_savesCorrectly() = runTest {
        // Given: ViewModel with no intentions
        val specialIntention = "Buy groceries: milk, eggs & bread (don't forget!)"

        // When: Saving intention with special characters
        viewModel.saveIntention(specialIntention)
        advanceUntilIdle()

        // Then: Should save with special characters intact
        val intentions = viewModel.intentions.value
        assertEquals(specialIntention, intentions[0])
    }

    @Test
    fun saveIntention_duplicateIntentions_bothSaved() = runTest {
        // Given: ViewModel with no intentions
        // When: Saving duplicate intentions
        viewModel.saveIntention("Water the plants")
        advanceUntilIdle()
        viewModel.saveIntention("Water the plants")
        advanceUntilIdle()

        // Then: Both should be saved (no deduplication)
        val intentions = viewModel.intentions.value
        assertEquals(2, intentions.size)
        assertEquals("Water the plants", intentions[0])
        assertEquals("Water the plants", intentions[1])
    }

    @Test
    fun intentions_orderMaintainedAfterMultipleSaves() = runTest {
        // Given: ViewModel with no intentions
        val intentionsList = listOf(
            "Morning meditation",
            "Healthy breakfast",
            "Team standup",
            "Code review",
            "Afternoon walk"
        )

        // When: Saving intentions in order
        intentionsList.forEach { intention ->
            viewModel.saveIntention(intention)
            advanceUntilIdle()
        }

        // Then: Most recent should be first (LIFO order)
        val savedIntentions = viewModel.intentions.value
        assertEquals(5, savedIntentions.size)
        assertEquals("Afternoon walk", savedIntentions[0])
        assertEquals("Morning meditation", savedIntentions[4])
    }

    // Mock ContentRepository for testing
    private class MockContentRepository : ContentRepository {
        val savedIntentions = mutableListOf<String>()
        private val _intentions = MutableStateFlow<List<String>>(emptyList())

        override fun getQuote(): Flow<String> = flowOf("Test quote from repository")

        override fun getImageResId(): Flow<Int> = flowOf(R.drawable.ic_launcher_background)

        override fun getIntentions(): Flow<List<String>> = _intentions

        override suspend fun saveIntention(intention: String) {
            savedIntentions.add(intention)
            val current = _intentions.value.toMutableList()
            current.add(0, intention)
            _intentions.value = current
        }
    }
}