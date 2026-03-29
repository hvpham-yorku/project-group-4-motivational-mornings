package com.example.motivationalmornings

import com.example.motivationalmornings.Presentation.AppDestinations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        viewModel = MainViewModel()
    }

    @Test
    fun initialDestination_isDailyContent() = runTest {
        // Given: ViewModel is initialized
        // When: Getting current destination
        val destination = viewModel.currentDestination.first()

        // Then: Default destination should be DAILY_CONTENT
        assertEquals(AppDestinations.DAILY_CONTENT, destination)
    }

    @Test
    fun setCurrentDestination_updatesToDashboard() = runTest {
        // Given: ViewModel is initialized with DAILY_CONTENT
        // When: Setting destination to DASHBOARD
        viewModel.setCurrentDestination(AppDestinations.DASHBOARD)

        // Then: Current destination should be DASHBOARD
        assertEquals(AppDestinations.DASHBOARD, viewModel.currentDestination.value)
    }

    @Test
    fun setCurrentDestination_updatesToAggregator() = runTest {
        // Given: ViewModel is initialized
        // When: Setting destination to AGGREGATOR
        viewModel.setCurrentDestination(AppDestinations.AGGREGATOR)

        // Then: Current destination should be AGGREGATOR
        assertEquals(AppDestinations.AGGREGATOR, viewModel.currentDestination.value)
    }

    @Test
    fun setCurrentDestination_updatesToRssFeed() = runTest {
        // Given: ViewModel is initialized
        // When: Setting destination to RSS_FEED
        viewModel.setCurrentDestination(AppDestinations.RSS_FEED)

        // Then: Current destination should be RSS_FEED
        assertEquals(AppDestinations.RSS_FEED, viewModel.currentDestination.value)
    }

    @Test
    fun setCurrentDestination_canChangeMultipleTimes() = runTest {
        // Given: ViewModel is initialized
        // When: Changing destinations multiple times
        viewModel.setCurrentDestination(AppDestinations.DASHBOARD)
        assertEquals(AppDestinations.DASHBOARD, viewModel.currentDestination.value)

        viewModel.setCurrentDestination(AppDestinations.RSS_FEED)
        assertEquals(AppDestinations.RSS_FEED, viewModel.currentDestination.value)

        viewModel.setCurrentDestination(AppDestinations.DAILY_CONTENT)
        // Then: Should return to DAILY_CONTENT
        assertEquals(AppDestinations.DAILY_CONTENT, viewModel.currentDestination.value)
    }
}
