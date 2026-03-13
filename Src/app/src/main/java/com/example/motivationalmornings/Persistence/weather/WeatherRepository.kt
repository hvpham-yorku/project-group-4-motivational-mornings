package com.example.motivationalmornings.Persistence.weather

interface WeatherRepository {
    suspend fun getCurrentWeather(): WeatherInfo
}

class HardcodedWeatherRepository : WeatherRepository {
    override suspend fun getCurrentWeather(): WeatherInfo {
        return WeatherInfo(
            temperatureC = 12.0,
            windSpeedKmh = 8.0,
            condition = "Clear"
        )
    }
}