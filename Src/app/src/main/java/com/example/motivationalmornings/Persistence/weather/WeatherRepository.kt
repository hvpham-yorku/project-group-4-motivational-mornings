package com.example.motivationalmornings.Persistence.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface WeatherRepository {
    suspend fun getCurrentWeather(city: String): WeatherInfo
}

class OpenMeteoWeatherRepository : WeatherRepository {

    private val geoRetrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geocodingApi = geoRetrofit.create(GeocodingApi::class.java)
    private val weatherApi = weatherRetrofit.create(WeatherApi::class.java)

    override suspend fun getCurrentWeather(city: String): WeatherInfo {
        // 1) City -> lat/lon
        val geo = geocodingApi.searchCity(city)
        val first = geo.results?.firstOrNull()
            ?: return WeatherInfo(0.0, 0.0, "City not found")

        val lat = first.latitude ?: return WeatherInfo(0.0, 0.0, "City not found")
        val lon = first.longitude ?: return WeatherInfo(0.0, 0.0, "City not found")

        // 2) lat/lon -> current weather
        val wx = weatherApi.getCurrentWeather(latitude = lat, longitude = lon, currentWeather = true)
        val current = wx.currentWeather
            ?: return WeatherInfo(0.0, 0.0, "No weather data")

        val temp = current.temperature ?: 0.0
        val wind = current.windspeed ?: 0.0
        val condition = codeToCondition(current.weathercode)

        return WeatherInfo(
            temperatureC = temp,
            windSpeedKmh = wind,
            condition = condition
        )
    }

    private fun codeToCondition(code: Int?): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67 -> "Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }
}