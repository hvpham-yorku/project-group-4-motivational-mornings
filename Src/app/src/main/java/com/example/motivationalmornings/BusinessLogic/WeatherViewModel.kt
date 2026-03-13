package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.weather.OpenMeteoWeatherRepository
import com.example.motivationalmornings.Persistence.weather.WeatherInfo
import com.example.motivationalmornings.Persistence.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repo: WeatherRepository = OpenMeteoWeatherRepository()
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // default city (you can change this)
    private val _city = MutableStateFlow("Toronto")
    val city: StateFlow<String> = _city.asStateFlow()

    fun setCity(newCity: String) {
        _city.value = newCity
    }

    fun loadWeather() {
        val cityToSearch = _city.value.trim()
        if (cityToSearch.isBlank()) {
            _error.value = "Enter a city name"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _weather.value = repo.getCurrentWeather(cityToSearch)
            } catch (e: Exception) {
                _error.value = "Failed to load weather"
            } finally {
                _isLoading.value = false
            }
        }
    }
}