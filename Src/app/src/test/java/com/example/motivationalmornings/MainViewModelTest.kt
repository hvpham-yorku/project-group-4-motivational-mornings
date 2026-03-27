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
    fun initialDestination_isDashboard() = runTest {
        // Given: ViewModel is initialized
        // When: Getting current destination
        val destination = viewModel.currentDestination.first()

        // Then: Default destination should be DASHBOARD
        assertEquals(AppDestinations.DASHBOARD, destination)
    }

    @Test
    fun setCurrentDestination_updatesToDailyContent() = runTest {
        // Given: ViewModel is initialized with DASHBOARD
        // When: Setting destination to DAILY_CONTENT
        viewModel.setCurrentDestination(AppDestinations.DAILY_CONTENT)

        // Then: Current destination should be DAILY_CONTENT
        assertEquals(AppDestinations.DAILY_CONTENT, viewModel.currentDestination.value)
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
        viewModel.setCurrentDestination(AppDestinations.DAILY_CONTENT)
        assertEquals(AppDestinations.DAILY_CONTENT, viewModel.currentDestination.value)

        viewModel.setCurrentDestination(AppDestinations.RSS_FEED)
        assertEquals(AppDestinations.RSS_FEED, viewModel.currentDestination.value)

        viewModel.setCurrentDestination(AppDestinations.DASHBOARD)
        // Then: Should return to DASHBOARD
        assertEquals(AppDestinations.DASHBOARD, viewModel.currentDestination.value)
    }
}