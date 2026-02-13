package com.example.motivationalmornings

import org.junit.Assert.assertEquals
import org.junit.Test

class RssFeedViewModelTest {
    @Test
    fun rssFeedText_defaultsToExpectedValue() {
        val vm = RssFeedViewModel()

        assertEquals("RSS Feed Screen", vm.rssFeedText.value)
    }
}

