package com.example.cartrack.core.di

import android.content.Context
import com.example.cartrack.core.storage.TokenManager // Import TokenManager
import com.example.cartrack.core.storage.TokenManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.auth.* // Import Auth
import io.ktor.client.plugins.auth.providers.* // Import BearerTokens & BearerAuthConfig
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.firstOrNull // Import firstOrNull
import kotlinx.coroutines.runBlocking // Import runBlocking
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
                // Logging (keep as before)
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL // Or LogLevel.HEADERS, LogLevel.BODY
                }
                // Content Negotiation (keep as before)
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
                        // Define how to load the token when needed for a request
                        loadTokens {
                            // Fetch the token from DataStore.
                            // NOTE: loadTokens runs synchronously within Ktor's pipeline.
                            // We use runBlocking here, which is acceptable for this specific
                            // use case as DataStore reads are fast after the first time.
                            // Fetching the first value ensures we get the current token.
                            val token = runBlocking { tokenManager.tokenFlow.firstOrNull() }
                            if (token != null) {
                                BearerTokens(
                                    accessToken = token,
                                    refreshToken = ""
                                ) // Provide empty refresh token if not used
                            } else {
                                null // Return null if no token is available
                            }
                        }

                        // Optional: Define how to refresh the token if your backend supports it
                        // refreshTokens { // current BearerTokens instance is 'oldTokens' } -> BearerTokens?
                        //    // TODO: Implement token refresh logic if needed
                        //    // Call your refresh endpoint, save new tokens, return new BearerTokens
                        //    null // Return null if refresh fails or isn't supported
                        // }

                        // Optional: Only send bearer token for specific hosts or paths
                        // sendWithoutRequest { request ->
                        //    request.url.host == "10.0.2.2" // Only send for your API host
                        // }
                    }
                }

                // Engine config (keep as before)
                engine {
                    connectTimeout = 15_000 // Increase slightly if needed
                    socketTimeout = 15_000
                }
            }
        }

        @Singleton
        @Provides
        fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
            // No @Inject needed on TokenManagerImpl if provided here
            return TokenManagerImpl(context) // Pass context to TokenManagerImpl
        }
    }
}