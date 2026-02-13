package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination

    fun setCurrentDestination(destination: AppDestinations) {
        _currentDestination.value = destination
    }
}
