package com.example.motivationalmornings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.data.weather.WeatherInfo
import com.example.motivationalmornings.data.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repo: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    fun loadWeather() {
        viewModelScope.launch {
            _weather.value = repo.getCurrentWeather()
        }
    }
}
