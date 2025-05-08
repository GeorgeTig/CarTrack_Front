package com.example.cartrack.feature.auth.domain.repository
import android.util.Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.vehicle.data.api.VehicleApi
import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApi,
    private val tokenManager: TokenManager,
    private val apiServiceVehicle : VehicleApi,
    private val vehicleManeger: VehicleManager
) : AuthRepository {

    private val logTag = "AuthRepo"

    override suspend fun login(request: UserLoginRequest): Result<Unit> {
        return try {

            val rawToken = apiService.login(request)

            // Had some problems with the token being returned with quotes
            val cleanedToken = if (rawToken.length >= 2 && rawToken.startsWith("\"") && rawToken.endsWith("\""))
            {
                rawToken.substring(1, rawToken.length - 1)
            }
            else {
                rawToken
            }

            Log.d(logTag, "Received raw token: [$rawToken]")
            Log.d(logTag, "Cleaned token to save: [$cleanedToken]")

            if (cleanedToken.isNotBlank())
            {
                if (!cleanedToken.contains('.'))
                {
                    Log.w(logTag, "Cleaned token does not contain dots, might still be invalid JWT format.")
                }
                tokenManager.saveToken(cleanedToken)
                Result.success(Unit)
            }
            else {

                Log.w(logTag, "Token was effectively empty after cleaning.")
                Result.failure(Exception("Login successful but token was empty or invalid."))
            }
        } catch (e: ClientRequestException) { // 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Login failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Login failed. Please check your credentials."))
        } catch (e: ServerResponseException) { // 5xx errors
            val errorMsg = "Login failed: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during login. Please try again later."))
        } catch (e: IOException) { // Network errors
            val errorMsg = "Login failed: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) { // JSON parsing errors
            val errorMsg = "Login failed: Error parsing login response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing the server response."))
        } catch (e: Exception) { // Catch-all for other unexpected errors
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
        } catch (e: ClientRequestException) { // 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Registration failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Registration failed: ${e.response.status.description}."))
        } catch (e: ServerResponseException) { // 5xx errors
            val errorMsg = "Registration failed: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during registration. Please try again later."))
        } catch (e: IOException) { // Network errors
            val errorMsg = "Registration failed: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) { // JSON parsing errors
            val errorMsg = "Registration failed: Error parsing registration response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred parsing the server response."))
        } catch (e: Exception) { // Catch-all
            val errorMsg = "Registration failed: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("An unexpected error occurred during registration."))
        }
    }

    override suspend fun logout() {

        tokenManager.deleteToken()
        vehicleManeger.deleteLastVehicleId()
        Log.d(logTag, "Logout successful (local token deleted).")

    }

    override fun isLoggedIn(): Flow<Boolean> {

        return tokenManager.tokenFlow.map { token ->
            val loggedIn = !token.isNullOrBlank()
            loggedIn
        }
    }

    override fun hasVehicles(): Flow<Boolean> {

        return vehicleManeger.lastVehicleIdFlow.map { vehicleId ->
            val hasVehicles = vehicleId != null
            hasVehicles
        }
    }

    override suspend fun hasVehicles(clientId: Int): Result<Unit> {
        val vehicleId = vehicleManeger.lastVehicleIdFlow.firstOrNull();

        if (vehicleId == null )
        {

            var listVehicle = apiServiceVehicle.getVehiclesByClientId(clientId);

            if (listVehicle.result.isEmpty())
            {
                Log.d(logTag, "No vehicles found for client $clientId")
                return Result.failure(Exception("No vehicles found for client $clientId"))
            }
            else
            {
                Log.d(logTag, "Vehicles found for client $clientId")
                vehicleManeger.saveLastVehicleId(listVehicle.result[0].id);
                return  Result.success(Unit)
            }
        }
        else
        {
            Log.d(logTag, "Last vehicle ID found: $vehicleId")
            return  Result.success(Unit)
        }
    }
}