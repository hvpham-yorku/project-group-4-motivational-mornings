package com.example.motivationalmornings.BusinessLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationalmornings.Persistence.weather.HardcodedWeatherRepository
import com.example.motivationalmornings.Persistence.weather.WeatherInfo
import com.example.motivationalmornings.Persistence.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repo: WeatherRepository = HardcodedWeatherRepository()
) : ViewModel() {

    private val _weather = MutableStateFlow<WeatherInfo?>(null)
    val weather: StateFlow<WeatherInfo?> = _weather.asStateFlow()

    fun loadWeather() {
        viewModelScope.launch {
            _weather.value = repo.getCurrentWeather()
        }
    }
}