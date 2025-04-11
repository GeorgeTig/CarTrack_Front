package com.example.cartrack.feature.auth.domain.repository
import android.util.Log // Import Android Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.model.LoginResponse // Make sure LoginResponse is imported
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.* // For ClientRequestException, ServerResponseException
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import java.io.IOException // For network connectivity issues
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApi, // Inject the API interface
    private val tokenManager: TokenManager // Inject the TokenManager interface
) : AuthRepository { // Implement the AuthRepository interface

    private val logTag = "AuthRepo" // Tag for logging

    override suspend fun login(request: UserLoginRequest): Result<Unit> {
        return try {
            // 1. Call the API, get the LoginResponse object
            val rawToken = apiService.login(request) // This should return a String token

            // 3. Clean the token: Remove surrounding quotes if they exist
            val cleanedToken = if (rawToken.length >= 2 && rawToken.startsWith("\"") && rawToken.endsWith("\"")) {
                // Ensure length is at least 2 before calling substring
                rawToken.substring(1, rawToken.length - 1)
            } else {
                rawToken // Use as-is if no surrounding quotes or too short
            }

            // 4. Log both raw and cleaned tokens for debugging
            Log.d(logTag, "Received raw token: [$rawToken]")
            Log.d(logTag, "Cleaned token to save: [$cleanedToken]")

            // 5. Check and save the CLEANED token
            if (cleanedToken.isNotBlank()) {
                // Ensure the cleaned token actually looks like a JWT (optional but good)
                if (!cleanedToken.contains('.')) {
                    Log.w(logTag, "Cleaned token does not contain dots, might still be invalid JWT format.")
                    // Decide if you want to fail here or proceed cautiously
                    // Result.failure(Exception("Received invalid token format after cleaning."))
                }
                tokenManager.saveToken(cleanedToken) // Save the cleaned version
                Result.success(Unit)
            } else {
                // Token was blank either originally or after removing quotes
                Log.w(logTag, "Token was effectively empty after cleaning.")
                Result.failure(Exception("Login successful but token was empty or invalid."))
            }
        } catch (e: ClientRequestException) { // 4xx errors
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Login failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e) // Log exception too
            Result.failure(Exception("Login failed. Please check your credentials.")) // User-friendly
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
                Result.failure(Exception("Registration failed: ${response.status.description}.")) // Keep user message simpler
            }
        } catch (e: ClientRequestException) { // 4xx errors (e.g., email already exists)
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Registration failed: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Registration failed: ${e.response.status.description}.")) // Adjust message as needed
        } catch (e: ServerResponseException) { // 5xx errors
            val errorMsg = "Registration failed: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error during registration. Please try again later."))
        } catch (e: IOException) { // Network errors
            val errorMsg = "Registration failed: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error. Please check your connection."))
        } catch (e: SerializationException) { // JSON parsing errors (less likely for register if only status matters)
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
        // Simply delete the token locally
        tokenManager.deleteToken()
        Log.d(logTag, "Logout successful (local token deleted).")
        // No network call needed unless you need to invalidate the token server-side
    }

    override fun isLoggedIn(): Flow<Boolean> {
        // The user is logged in if the token exists and is not blank
        return tokenManager.tokenFlow.map { token ->
            val loggedIn = !token.isNullOrBlank()
            // Log.v(logTag, "isLoggedIn check: token present = ${token!=null}, isBlank = ${token?.isBlank()}, result = $loggedIn") // Verbose logging if needed
            loggedIn
        }
    }
}