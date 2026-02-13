package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AggregatorViewModel : ViewModel() {
    private val _aggregatorText = MutableStateFlow("Aggregator Screen")
    val aggregatorText: StateFlow<String> = _aggregatorText
}
