package com.example.cartrack.feature.auth.domain.repository

import android.util.Log
import com.example.cartrack.core.di.AuthenticatedAuthApi
import com.example.cartrack.core.di.UnauthenticatedAuthApi
import com.example.cartrack.core.storage.AuthTokens
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.data.api.VehicleApi
import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.model.RefreshTokenRequest
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.bodyAsText // Import necesar
import io.ktor.http.HttpStatusCode // Import necesar
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import io.ktor.serialization.JsonConvertException // Import specific
import kotlinx.serialization.SerializationException // Import general pentru kotlinx.serialization
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @AuthenticatedAuthApi private val authenticatedAuthApiService: AuthApi,
    @UnauthenticatedAuthApi private val unauthenticatedAuthApiService: AuthApi,
    private val tokenManager: TokenManager,
    private val vehicleApiService : VehicleApi,
    private val vehicleManager: VehicleManager,
    private val userManager: UserManager,
    private val jwtDecoder: JwtDecoder
) : AuthRepository {

    private val logTag = "AuthRepo"

    override suspend fun login(request: UserLoginRequest): Result<Unit> {
        return try {
            val tokenResponse = authenticatedAuthApiService.login(request)
            Log.d(logTag, "Login API call successful. AT: ...${tokenResponse.accessToken.takeLast(6)}, RT: ...${tokenResponse.refreshToken.takeLast(6)}")

            if (tokenResponse.accessToken.isNotBlank() && tokenResponse.refreshToken.isNotBlank()) {
                tokenManager.saveTokens(AuthTokens(tokenResponse.accessToken, tokenResponse.refreshToken))
                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId != null) {
                    userManager.saveClientId(clientId)
                    Log.d(logTag, "Client ID $clientId saved after login.")
                    Result.success(Unit)
                } else {
                    Log.e(logTag, "Failed to extract client ID from new accessToken after login.")
                    tokenManager.deleteTokens()
                    Result.failure(Exception("Login succeeded but new token is invalid."))
                }
            } else {
                Log.w(logTag, "Login response contained blank tokens.")
                Result.failure(Exception("Login successful but tokens were empty."))
            }
        } catch (e: ResponseException) { // MODIFICAT: Prinde ResponseException mai general
            val errorBody = kotlin.runCatching { e.response.bodyAsText() }.getOrNull()
            Log.e(logTag, "Login failed (HTTP Error ${e.response.status.value}). Body: $errorBody", e)
            if (e.response.status == HttpStatusCode.Unauthorized) {
                Result.failure(Exception("Invalid credentials. Please try again."))
            } else {
                Result.failure(Exception("Login failed (Code: ${e.response.status.value}). Please try again."))
            }
        } catch (e: IOException) {
            Log.e(logTag, "Login failed (Network Error): ${e.localizedMessage}", e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: JsonConvertException) {
            Log.e(logTag, "Login failed (JsonConvertException): ${e.message}. Likely malformed JSON or type mismatch.", e)
            Result.failure(Exception("Error processing server response for login."))
        }
        catch (e: SerializationException) {
            Log.e(logTag, "Login failed (Serialization Error): ${e.localizedMessage}", e)
            Result.failure(Exception("Error parsing server response for login."))
        }
        catch (e: Exception) {
            Log.e(logTag, "Login failed (Unexpected Error): ${e.localizedMessage}", e)
            Result.failure(Exception("An unexpected error occurred during login."))
        }
    }

    override suspend fun attemptSilentRefresh(): Result<Unit> {
        val currentRefreshToken = tokenManager.refreshTokenFlow.firstOrNull()
        if (currentRefreshToken.isNullOrBlank()) {
            Log.d(logTag, "No refresh token found. Silent refresh skipped.")
            return Result.failure(Exception("No refresh token available."))
        }

        Log.d(logTag, "Attempting silent refresh with RT: ...${currentRefreshToken.takeLast(6)}")
        try {
            val refreshTokenRequest = RefreshTokenRequest(currentRefreshToken)
            val newTokenResponse = unauthenticatedAuthApiService.refreshToken(refreshTokenRequest)

            // Acest bloc se execută doar dacă API-ul refreshToken a returnat un status de succes (2xx)
            // și Ktor a reușit să deserializeze corpul în TokenResponse.
            if (newTokenResponse.accessToken.isNotBlank() && newTokenResponse.refreshToken.isNotBlank()) {
                tokenManager.saveTokens(AuthTokens(newTokenResponse.accessToken, newTokenResponse.refreshToken))
                Log.i(logTag, "Silent refresh successful. Tokens updated.")
                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId != null) {
                    userManager.saveClientId(clientId)
                } else {
                    Log.e(logTag, "Failed to extract client ID from new token after silent refresh.")
                    tokenManager.deleteTokens()
                    return Result.failure(Exception("Silent refresh succeeded but new access token is invalid."))
                }
                return Result.success(Unit)
            } else {
                Log.w(logTag, "Silent refresh: Server returned 2xx but with blank tokens.")
                tokenManager.deleteTokens()
                return Result.failure(Exception("Refresh failed: Server returned empty tokens."))
            }
        } catch (e: ResponseException) { // NOU / MODIFICAT: Prinde excepții HTTP specifice
            val statusCode = e.response.status
            val errorBody = kotlin.runCatching { e.response.bodyAsText() }.getOrNull()
            Log.e(logTag, "Silent refresh failed (HTTP Error $statusCode from /refresh). Body: $errorBody", e)

            if (statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden) {
                // 401 sau 403 de la /refresh înseamnă că refreshToken-ul e invalid/expirat.
                // Serverul a răspuns corect cu un status de eroare.
                tokenManager.deleteTokens() // Șterge token-urile vechi
                return Result.failure(Exception("Your session has expired. Please login again. (Code: $statusCode)"))
            } else if (statusCode.value >= 500) {
                // Eroare server la /refresh.
                tokenManager.deleteTokens() // E mai sigur să ștergi token-urile
                return Result.failure(Exception("Could not refresh session due to a server error. Please login. (Code: $statusCode)"))
            }
            // Alte erori HTTP client (400, 404 etc.)
            return Result.failure(Exception("Failed to refresh session. (Code: $statusCode)"))
        } catch (e: JsonConvertException) { // NOU: Prinde specific eroarea de deserializare
            // Aceasta se poate întâmpla dacă serverul returnează 2xx (ceea ce nu ar trebui dacă e eroare)
            // SAU dacă setările Ktor (ex: `expectSuccess = true`) nu sunt configurate să arunce ResponseException pentru non-2xx
            // și apoi încearcă să deserializeze un corp de eroare (ex: JSON cu `{"message":...}`) în `TokenResponse`.
            // Logul tău anterior arată că `JsonConvertException` este aruncată chiar dacă statusul era 401,
            // ceea ce e un pic ciudat, dar o prindem oricum.
            val responseContent = (e.cause as? Exception)?.message ?: e.message // Încearcă să obții detalii
            Log.e(logTag, "Silent Refresh failed (JsonConvertException): $responseContent. Likely server returned error JSON instead of TokenResponse.", e)
            tokenManager.deleteTokens() // Eșec la parsare, consideră sesiunea invalidă
            return Result.failure(Exception("Server returned an unexpected response format during session refresh. Please login."))
        }
        catch (e: IOException) {
            Log.e(logTag, "Silent refresh failed (Network Error): ${e.localizedMessage}", e)
            return Result.failure(Exception("Network error during session refresh. Please check your connection."))
        }
        catch (e: Exception) { // Prindere generală pentru orice altceva
            Log.e(logTag, "Silent refresh failed (Unexpected Error): ${e.message}", e)
            tokenManager.deleteTokens()
            return Result.failure(Exception("Could not refresh session: ${e.message}. Please login."))
        }
    }

    // ... restul metodelor (register, logout, isLoggedIn, etc.) rămân la fel ...
    override suspend fun register(request: UserRegisterRequest): Result<Unit> {
        return try {
            val response = authenticatedAuthApiService.register(request)
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull()
                Log.w(logTag, "Registration failed: ${response.status.description} (${response.status.value}). Body: $errorBody")
                Result.failure(Exception("Registration failed: ${response.status.description}."))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Registration failed: ${e.localizedMessage}", e)
            Result.failure(Exception("An unexpected error occurred during registration: ${e.message}"))
        }
    }

    override suspend fun logout() {
        tokenManager.deleteTokens()
        vehicleManager.deleteLastVehicleId()
        userManager.clearUserData()
        Log.d(logTag, "Logout successful.")
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.accessTokenFlow.map { accessToken ->
            !accessToken.isNullOrBlank()
        }
    }

    override fun hasVehicles(): Flow<Boolean> {
        return vehicleManager.lastVehicleIdFlow.map { vehicleId ->
            vehicleId != null
        }
    }

    override suspend fun hasVehicles(clientId: Int): Result<Unit> {
        val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
        if (vehicleId == null) {
            return try {
                val listVehicle = vehicleApiService.getVehiclesByClientId(clientId)
                if (listVehicle.result.isEmpty()) {
                    Log.d(logTag, "No vehicles found for client $clientId")
                    Result.failure(Exception("No vehicles found for client $clientId"))
                } else {
                    Log.d(logTag, "Vehicles found for client $clientId, saving first ID: ${listVehicle.result[0].id}")
                    vehicleManager.saveLastVehicleId(listVehicle.result[0].id)
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                Log.e(logTag, "Error fetching vehicles for client $clientId: ${e.message}", e)
                Result.failure(Exception("Error checking for vehicles: ${e.message}"))
            }
        } else {
            Log.d(logTag, "Last vehicle ID $vehicleId found in cache.")
            return Result.success(Unit)
        }
    }
}