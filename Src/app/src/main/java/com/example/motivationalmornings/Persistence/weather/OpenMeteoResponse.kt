package com.example.motivationalmornings.Persistence.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather?
)

data class CurrentWeather(
    val temperature: Double?,
    val windspeed: Double?,
    val weathercode: Int?
)