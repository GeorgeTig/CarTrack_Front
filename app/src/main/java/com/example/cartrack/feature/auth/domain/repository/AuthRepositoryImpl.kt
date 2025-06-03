package com.example.cartrack.feature.auth.domain.repository

import android.util.Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.data.api.VehicleApi
import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApi,
    private val tokenManager: TokenManager,
    private val apiServiceVehicle : VehicleApi,
    private val vehicleManager: VehicleManager, // Renamed for consistency
    private val userManager: UserManager,       // Injected UserManager
    private val jwtDecoder: JwtDecoder          // Injected JwtDecoder
) : AuthRepository {

    private val logTag = "AuthRepo"

    override suspend fun login(request: UserLoginRequest): Result<Unit> {
        return try {
            val rawToken = apiService.login(request)
            val cleanedToken = if (rawToken.length >= 2 && rawToken.startsWith("\"") && rawToken.endsWith("\"")) {
                rawToken.substring(1, rawToken.length - 1)
            } else {
                rawToken
            }

            Log.d(logTag, "Received raw token: [$rawToken]")
            Log.d(logTag, "Cleaned token to save: [$cleanedToken]")

            if (cleanedToken.isNotBlank()) {
                if (!cleanedToken.contains('.')) {
                    Log.w(logTag, "Cleaned token does not contain dots, might still be invalid JWT format.")
                }
                tokenManager.saveToken(cleanedToken)

                // Save client ID after successful login and token save
                val clientId = jwtDecoder.getClientIdFromToken() // Use the injected JwtDecoder
                if (clientId != null) {
                    userManager.saveClientId(clientId)
                    Log.d(logTag, "Client ID $clientId saved via UserManager.")
                } else {
                    Log.e(logTag, "Failed to extract client ID from token after login.")
                    // Decide if this is a critical failure for login
                    // For now, we proceed, but this might need to be handled as Result.failure
                }
                Result.success(Unit)
            } else {
                Log.w(logTag, "Token was effectively empty after cleaning.")
                Result.failure(Exception("Login successful but token was empty or invalid."))
            }
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Login failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Login failed. Please check your credentials."))
        } catch (e: ServerResponseException) {
            val errorMsg = "Login failed: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during login. Please try again later."))
        } catch (e: IOException) {
            val errorMsg = "Login failed: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) {
            val errorMsg = "Login failed: Error parsing login response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing the server response."))
        } catch (e: Exception) {
            val errorMsg = "Login failed: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred during login."))
        }
    }

    override suspend fun register(request: UserRegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(request)
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = runCatching { response.body<String>() }.getOrNull()
                val errorMsg = "Registration failed: ${response.status.description} (${response.status.value}). ${errorBody ?: ""}"
                Log.w(logTag, errorMsg)
                Result.failure(Exception("Registration failed: ${response.status.description}."))
            }
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Registration failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Registration failed: ${e.response.status.description}."))
        } catch (e: ServerResponseException) {
            val errorMsg = "Registration failed: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during registration. Please try again later."))
        } catch (e: IOException) {
            val errorMsg = "Registration failed: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) {
            val errorMsg = "Registration failed: Error parsing registration response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing the server response."))
        } catch (e: Exception) {
            val errorMsg = "Registration failed: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred during registration."))
        }
    }

    override suspend fun logout() {
        tokenManager.deleteToken()
        vehicleManager.deleteLastVehicleId()
        userManager.clearUserData()
        Log.d(logTag, "Logout successful (local token, vehicle cache, and user prefs deleted).")
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.tokenFlow.map { token ->
            val loggedIn = !token.isNullOrBlank()
            loggedIn
        }
    }

    override fun hasVehicles(): Flow<Boolean> {
        return vehicleManager.lastVehicleIdFlow.map { vehicleId ->
            val hasVehicles = vehicleId != null
            hasVehicles
        }
    }

    override suspend fun hasVehicles(clientId: Int): Result<Unit> {
        val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()

        if (vehicleId == null ) {
            return try {
                val listVehicle = apiServiceVehicle.getVehiclesByClientId(clientId)
                if (listVehicle.result.isEmpty()) {
                    Log.d(logTag, "No vehicles found for client $clientId")
                    Result.failure(Exception("No vehicles found for client $clientId"))
                } else {
                    Log.d(logTag, "Vehicles found for client $clientId, saving first ID: ${listVehicle.result[0].id}")
                    vehicleManager.saveLastVehicleId(listVehicle.result[0].id)
                    Result.success(Unit)
                }
            } catch (e: Exception) { // Catch exceptions from API call
                Log.e(logTag, "Error fetching vehicles for client $clientId in hasVehicles: ${e.message}", e)
                Result.failure(Exception("Error checking for vehicles: ${e.message}"))
            }
        } else {
            Log.d(logTag, "Last vehicle ID found in cache: $vehicleId")
            return Result.success(Unit)
        }
    }
}