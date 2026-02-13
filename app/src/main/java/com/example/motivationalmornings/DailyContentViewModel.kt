package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DailyContentViewModel : ViewModel() {
    private val _quote = MutableStateFlow("\"The best way to predict the future is to create it.\"")
    val quote: StateFlow<String> = _quote

    private val _imageResId = MutableStateFlow(R.drawable.ic_launcher_background)
    val imageResId: StateFlow<Int> = _imageResId

    private val _intentions = MutableStateFlow("My intentions for today are...")
    val intentions: StateFlow<String> = _intentions
}
