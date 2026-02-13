package com.example.motivationalmornings

import com.example.motivationalmornings.data.ContentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailyContentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun quote_collectsFromRepository() = runTest {
        val repo = FakeContentRepository(
            quoteFlow = flowOf("Test quote"),
        )
        val vm = DailyContentViewModel(repo)

        assertEquals("Test quote", vm.quote.first { it.isNotBlank() })
    }

    @Test
    fun imageResId_collectsFromRepository() = runTest {
        val repo = FakeContentRepository(
            imageResIdFlow = flowOf(1234),
        )
        val vm = DailyContentViewModel(repo)

        assertEquals(1234, vm.imageResId.first { it != R.drawable.ic_launcher_background })
    }

    @Test
    fun saveIntention_delegatesToRepository() = runTest {
        val intentions = MutableStateFlow("")
        val repo = FakeContentRepository(intentionsFlow = intentions)
        val vm = DailyContentViewModel(repo)

        vm.saveIntention("Be present.")
        advanceUntilIdle()

        assertEquals("Be present.", intentions.value)
        assertEquals("Be present.", vm.intentions.first())
    }

    private class FakeContentRepository(
        private val quoteFlow: Flow<String> = flowOf(""),
        private val imageResIdFlow: Flow<Int> = flowOf(R.drawable.ic_launcher_background),
        private val intentionsFlow: MutableStateFlow<String> = MutableStateFlow(""),
    ) : ContentRepository {
        override fun getQuote(): Flow<String> = quoteFlow
        override fun getImageResId(): Flow<Int> = imageResIdFlow
        override fun getIntentions(): Flow<String> = intentionsFlow
        override suspend fun saveIntention(intention: String) {
            intentionsFlow.value = intention
        }
    }
}

