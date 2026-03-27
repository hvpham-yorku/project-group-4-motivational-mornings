package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import com.example.motivationalmornings.Presentation.AppDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _currentDestination = MutableStateFlow<AppDestinations>(AppDestinations.DASHBOARD)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()

    fun setCurrentDestination(destination: AppDestinations) {
        _currentDestination.value = destination
    }
}
