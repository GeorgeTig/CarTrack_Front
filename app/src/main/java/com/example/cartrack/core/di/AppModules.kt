package com.example.cartrack.core.di

import android.content.Context
import android.util.Log
import com.example.cartrack.core.data.api.AuthApiImpl
import com.example.cartrack.core.data.model.auth.RefreshTokenRequestDto
import com.example.cartrack.core.storage.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModules {

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
    fun provideAuthenticatedHttpClient(
        tokenManager: TokenManager,
        @UnauthenticatedHttpClient unauthClient: HttpClient
    ): HttpClient {
        val authApiForRefresh = AuthApiImpl(unauthClient)

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
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenManager.getTokens()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                    }
                    refreshTokens {
                        tokenManager.getTokens()?.refreshToken?.let { rt ->
                            try {
                                val newTokenResponse = authApiForRefresh.refreshToken(RefreshTokenRequestDto(rt))
                                val newAuthTokens = AuthTokens(newTokenResponse.accessToken, newTokenResponse.refreshToken)
                                tokenManager.saveTokens(newAuthTokens)
                                BearerTokens(newAuthTokens.accessToken, newAuthTokens.refreshToken)
                            } catch (e: Exception) {
                                Log.e("HttpClientAuth", "[KTOR_AUTH] Refresh failed, deleting tokens", e)
                                tokenManager.deleteTokens()
                                null
                            }
                        }
                    }
                }
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 15_000
            }
        }
    }

    @Singleton @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager = TokenManagerImpl(context)

    @Singleton @Provides
    fun provideVehicleManager(@ApplicationContext context: Context): VehicleManager = VehicleManagerImpl(context)

    @Singleton @Provides
    fun provideUserManager(@ApplicationContext context: Context): UserManager = UserManagerImpl(context)
}