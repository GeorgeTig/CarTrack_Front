package com.example.cartrack.core.di

import android.content.Context
import android.util.Log
import com.example.cartrack.core.storage.AuthTokens
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.TokenManagerImpl
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.storage.UserManagerImpl
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.storage.VehicleManagerImpl
import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.api.AuthApiImpl
import com.example.cartrack.feature.auth.data.model.RefreshTokenRequest
// Nu mai importăm VehicleApi/Impl aici dacă e gestionat de VehicleModule
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
        val authApiForKtorRefreshMechanism = AuthApiImpl(unauthClient)
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
                        val currentTokens = tokenManager.getTokens()
                        if (currentTokens != null) {
                            Log.d("HttpClientAuth", "[KTOR_AUTH] loadTokens: AT loaded: ...${currentTokens.accessToken.takeLast(6)}")
                            BearerTokens(currentTokens.accessToken, currentTokens.refreshToken)
                        } else {
                            Log.d("HttpClientAuth", "[KTOR_AUTH] loadTokens: No tokens found.")
                            null
                        }
                    }
                    refreshTokens {
                        val oldTokens = tokenManager.getTokens()
                        val refreshTokenString = oldTokens?.refreshToken
                        if (refreshTokenString.isNullOrBlank()) {
                            Log.w("HttpClientAuth", "[KTOR_AUTH] refreshTokens: No refresh token available for Ktor's auto-refresh.")
                            return@refreshTokens null
                        }
                        Log.d("HttpClientAuth", "[KTOR_AUTH] refreshTokens: Attempting with RT: ...${refreshTokenString.takeLast(6)}")
                        try {
                            val newTokenResponse = authApiForKtorRefreshMechanism.refreshToken(
                                RefreshTokenRequest(refreshTokenString)
                            )
                            tokenManager.saveTokens(
                                AuthTokens(newTokenResponse.accessToken, newTokenResponse.refreshToken)
                            )
                            Log.i("HttpClientAuth", "[KTOR_AUTH] refreshTokens: Success. New AT: ...${newTokenResponse.accessToken.takeLast(6)}")
                            BearerTokens(newTokenResponse.accessToken, newTokenResponse.refreshToken)
                        } catch (e: Exception) {
                            Log.e("HttpClientAuth", "[KTOR_AUTH] refreshTokens: Failed: ${e.message}", e)
                            tokenManager.deleteTokens()
                            null
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

    @Provides
    @Singleton
    @AuthenticatedAuthApi
    fun provideAuthenticatedAuthApi(@AuthenticatedHttpClient client: HttpClient): AuthApi {
        return AuthApiImpl(client)
    }

    @Provides
    @Singleton
    @UnauthenticatedAuthApi
    fun provideUnauthenticatedAuthApi(@UnauthenticatedHttpClient client: HttpClient): AuthApi {
        return AuthApiImpl(client)
    }

    // Am eliminat provideVehicleApi de aici pentru a evita duplicarea.
    // VehicleModule se va ocupa de bind-ul pentru VehicleApi.

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

    @Singleton
    @Provides
    fun provideUserManager(@ApplicationContext context: Context): UserManager {
        return UserManagerImpl(context)
    }
}
