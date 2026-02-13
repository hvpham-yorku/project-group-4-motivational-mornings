package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RssFeedViewModel : ViewModel() {
    private val _rssFeedText = MutableStateFlow("RSS Feed Screen")
    val rssFeedText: StateFlow<String> = _rssFeedText
}
