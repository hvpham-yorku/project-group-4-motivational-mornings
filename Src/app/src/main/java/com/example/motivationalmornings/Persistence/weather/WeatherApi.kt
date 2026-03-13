package com.example.motivationalmornings.Persistence.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    // Open-Meteo: https://api.open-meteo.com/v1/forecast
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}