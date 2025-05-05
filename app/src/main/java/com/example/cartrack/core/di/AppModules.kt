package com.example.cartrack.core.di

import android.content.Context
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.TokenManagerImpl
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.storage.VehicleManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModules {

    companion object {
        @Provides
        @Singleton

        fun provideHttpClient(tokenManager: TokenManager): HttpClient {
            return HttpClient(Android) {
                // Logging
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
                // Content Negotiation
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }

                // *** Install Auth Plugin for Bearer Tokens ***
                install(Auth) {
                    bearer {
                        loadTokens {
                            val token = runBlocking { tokenManager.tokenFlow.firstOrNull() }
                            if (token != null) {
                                BearerTokens(
                                    accessToken = token,
                                    refreshToken = ""
                                )
                            } else {
                                null
                            }
                        }

                    }
                }

                // Engine config
                engine {
                    connectTimeout = 15_000
                    socketTimeout = 15_000
                }
            }
        }

        @Singleton
        @Provides
        fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
            return TokenManagerImpl(context)
        }

        @Singleton
        @Provides
        fun provideVehicleManager(@ApplicationContext context: Context): VehicleManager {
            return VehicleManagerImpl(context)
        }
    }
}