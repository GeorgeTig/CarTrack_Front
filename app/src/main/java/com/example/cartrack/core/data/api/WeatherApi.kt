package com.example.cartrack.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Definim doar datele de care avem nevoie
@Serializable
data class WeatherResponse(
    @SerialName("weather") val weatherInfo: List<WeatherInfo>,
    @SerialName("main") val main: MainInfo,
    @SerialName("name") val cityName: String
)

@Serializable
data class WeatherInfo(
    val description: String,
    val icon: String
)

@Serializable
data class MainInfo(
    @SerialName("temp") val temperature: Double
)

// Interfața nu este neapărat necesară pentru un singur apel,
// dar o adăugăm pentru consistență.
interface WeatherApi {
    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse
}