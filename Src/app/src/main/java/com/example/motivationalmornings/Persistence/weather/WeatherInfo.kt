package com.example.motivationalmornings.Persistence.weather

data class WeatherInfo(
    val temperatureC: Double,
    val windSpeedKmh: Double,
    val condition: String
)