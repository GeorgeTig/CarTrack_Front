package com.example.cartrack.core.data.api

import com.example.cartrack.core.di.UnauthenticatedHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class WeatherApiImpl @Inject constructor(
    @UnauthenticatedHttpClient private val client: HttpClient
) : WeatherApi {

    private val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"

    override suspend fun getWeather(lat: Double, lon: Double, apiKey: String): WeatherResponse {
        return client.get(BASE_URL) {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("appid", apiKey)
            parameter("units", "metric") // Pentru a primi temperatura în Celsius
        }.body()
    }
}