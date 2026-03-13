package com.example.motivationalmornings.Persistence.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    // Open-Meteo Geocoding: https://geocoding-api.open-meteo.com/v1/search?name=Toronto&count=1
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

data class GeocodingResult(
    val name: String?,
    val latitude: Double?,
    val longitude: Double?
)