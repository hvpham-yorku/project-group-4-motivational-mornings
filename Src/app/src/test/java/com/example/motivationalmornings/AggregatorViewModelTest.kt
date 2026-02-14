package com.example.motivationalmornings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AggregatorViewModelTest {

    private lateinit var viewModel: AggregatorViewModel

    @Before
    fun setup() {
        viewModel = AggregatorViewModel()
    }

    @Test
    fun initialAggregatorText_isAggregatorScreen() = runTest {
        // Given: ViewModel is initialized
        // When: Getting aggregator text
        val text = viewModel.aggregatorText.first()

        // Then: Text should be "Aggregator Screen"
        assertEquals("Aggregator Screen", text)
    }

    @Test
    fun aggregatorText_remainsConstant() = runTest {
        // Given: ViewModel is initialized
        val initialText = viewModel.aggregatorText.first()

        // When: Collecting text again
        val subsequentText = viewModel.aggregatorText.value

        // Then: Text should remain the same
        assertEquals(initialText, subsequentText)
        assertEquals("Aggregator Screen", subsequentText)
    }
}