package com.example.cartrack.core.data.repository

import android.util.Log
import com.example.cartrack.core.data.api.AuthApi
import com.example.cartrack.core.data.model.auth.RefreshTokenRequestDto
import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import com.example.cartrack.core.di.AuthenticatedAuthApi
import com.example.cartrack.core.di.UnauthenticatedAuthApi
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.AuthTokens
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.storage.VehicleManager
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @AuthenticatedAuthApi private val authenticatedAuthApi: AuthApi,
    @UnauthenticatedAuthApi private val unauthenticatedAuthApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val vehicleManager: VehicleManager,
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder
) : AuthRepository {

    private val logTag = "AuthRepo"

    override suspend fun login(request: UserLoginRequestDto): Result<Unit> {
        return try {
            val tokenResponse = unauthenticatedAuthApi.login(request)
            Log.d(logTag, "Login API call successful.")

            val clientId = jwtDecoder.getClientIdFromToken(tokenResponse.accessToken)
            if (clientId != null) {
                tokenManager.saveTokens(AuthTokens(tokenResponse.accessToken, tokenResponse.refreshToken))
                userManager.saveClientId(clientId)
                Log.d(logTag, "Client ID $clientId and tokens saved after login.")
                Result.success(Unit)
            } else {
                Log.e(logTag, "Failed to extract client ID from new accessToken after login.")
                Result.failure(Exception("Login succeeded but new token is invalid."))
            }
        } catch (e: ResponseException) {
            val errorBody = runCatching { e.response.bodyAsText() }.getOrNull()
            Log.e(logTag, "Login failed (HTTP Error ${e.response.status.value}). Body: $errorBody", e)
            if (e.response.status == HttpStatusCode.Unauthorized) {
                Result.failure(Exception("Invalid credentials. Please try again."))
            } else {
                Result.failure(Exception("Login failed (Code: ${e.response.status.value})."))
            }
        } catch (e: IOException) {
            Log.e(logTag, "Login failed (Network Error): ${e.localizedMessage}", e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: Exception) {
            Log.e(logTag, "Login failed (Unexpected Error): ${e.localizedMessage}", e)
            Result.failure(Exception("An unexpected error occurred during login."))
        }
    }

    override suspend fun register(request: UserRegisterRequestDto): Result<Unit> {
        return try {
            val response = unauthenticatedAuthApi.register(request)
            if (response.status.isSuccess()) {
                Log.d(logTag, "Registration successful.")
                Result.success(Unit)
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull()
                Log.w(logTag, "Registration failed: ${response.status}. Body: $errorBody")
                Result.failure(Exception("Registration failed: ${response.status.description}."))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Registration failed: ${e.message}", e)
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }

    override suspend fun attemptSilentRefresh(): Result<Unit> {
        val refreshToken = tokenManager.refreshTokenFlow.firstOrNull()
            ?: return Result.failure(Exception("No refresh token available."))

        Log.d(logTag, "Attempting silent refresh.")
        return try {
            val response = unauthenticatedAuthApi.refreshToken(RefreshTokenRequestDto(refreshToken))
            val clientId = jwtDecoder.getClientIdFromToken(response.accessToken)
            if (clientId != null) {
                tokenManager.saveTokens(AuthTokens(response.accessToken, response.refreshToken))
                userManager.saveClientId(clientId)
                Log.i(logTag, "Silent refresh successful. Tokens updated.")
                Result.success(Unit)
            } else {
                Log.e(logTag, "Silent refresh: new token is invalid.")
                tokenManager.deleteTokens() // Șterge token-urile invalide
                Result.failure(Exception("Session invalid."))
            }
        } catch (e: ResponseException) {
            Log.e(logTag, "Silent refresh failed (HTTP Error ${e.response.status.value}).", e)
            tokenManager.deleteTokens() // Token-ul de refresh este probabil expirat/invalid
            Result.failure(Exception("Your session has expired. Please login again."))
        } catch (e: Exception) {
            Log.e(logTag, "Silent refresh failed (Unexpected Error).", e)
            Result.failure(Exception("Could not refresh session: ${e.message}"))
        }
    }

    override suspend fun logout() {
        tokenManager.deleteTokens()
        userManager.clearUserData()
        vehicleManager.deleteLastVehicleId()
        Log.d(logTag, "Logout successful: All user data cleared.")
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.accessTokenFlow.map { !it.isNullOrBlank() }
    }

    override suspend fun hasVehicles(clientId: Int): Result<Unit> {
        val result = vehicleRepository.getVehiclesByClientId(clientId)
        return result.mapCatching { vehicles ->
            if (vehicles.isEmpty()) {
                throw Exception("No vehicles found for this user.")
            } else {
                // Dacă nu există un vehicul salvat, salvăm primul din listă
                val lastUsedId = vehicleManager.lastVehicleIdFlow.firstOrNull()
                if (lastUsedId == null || vehicles.none { it.id == lastUsedId }) {
                    vehicleManager.saveLastVehicleId(vehicles.first().id)
                }
            }
        }
    }
}