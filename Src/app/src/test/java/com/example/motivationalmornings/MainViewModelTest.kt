package com.example.motivationalmornings

import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {
    @Test
    fun currentDestination_defaultsToHome() {
        val vm = MainViewModel()

        assertEquals(AppDestinations.HOME, vm.currentDestination.value)
    }

    @Test
    fun setCurrentDestination_updatesFlow() {
        val vm = MainViewModel()

        vm.setCurrentDestination(AppDestinations.RSS_FEED)

        assertEquals(AppDestinations.RSS_FEED, vm.currentDestination.value)
    }
}

