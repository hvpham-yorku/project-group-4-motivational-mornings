package com.example.motivationalmornings

import org.junit.Assert.assertEquals
import org.junit.Test

class AggregatorViewModelTest {
    @Test
    fun aggregatorText_defaultsToExpectedValue() {
        val vm = AggregatorViewModel()

        assertEquals("Aggregator Screen", vm.aggregatorText.value)
    }
}

