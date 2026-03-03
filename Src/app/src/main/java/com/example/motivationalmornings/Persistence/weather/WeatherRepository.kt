package com.example.motivationalmornings.data.weather

class WeatherRepository {
    suspend fun getCurrentWeather(): WeatherInfo {
        return WeatherInfo(
            temperatureC = 12.0,
            windSpeedKmh = 8.0,
            condition = "Clear"
        )
    }
}