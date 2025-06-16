package com.example.cartrack.core.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HttpClientModule {

    @Provides
    @Singleton
    @UnauthenticatedHttpClient
    fun provideUnauthenticatedHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger { override fun log(message: String) { Log.v("KtorUnauthLogger", message) } }
                level = LogLevel.BODY
            }
            engine {
                connectTimeout = 10_000
                socketTimeout = 10_000
            }
        }
    }

    @Provides
    @Singleton
    @AuthenticatedHttpClient
    fun provideAuthenticatedHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(Logging) {
                logger = object : Logger { override fun log(message: String) { Log.v("KtorAuthLogger", message) } }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 15_000
            }
        }
    }
}