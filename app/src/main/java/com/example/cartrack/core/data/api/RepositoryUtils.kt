package com.example.cartrack.core.data.api

import android.util.Log
import com.example.cartrack.core.domain.repository.AuthRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Provider

/**
 * Execută un apel API într-un mod sigur, gestionând erorile comune și reîmprospătarea token-ului.
 *
 * @param authRepositoryProvider Provider pentru AuthRepository, folosit pentru a încerca reîmprospătarea token-ului.
 * @param endpointName Un nume descriptiv pentru endpoint, folosit în log-uri.
 * @param apiCall Lambda-ul care conține apelul API efectiv.
 * @return Un obiect [Result] care conține fie succesul (T), fie o excepție.
 */
suspend fun <T> safeApiCall(
    authRepositoryProvider: Provider<AuthRepository>,
    endpointName: String,
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: ResponseException) {
        if (e.response.status == HttpStatusCode.Unauthorized) {
            Log.w("SafeApiCall", "Unauthorized access to $endpointName. Attempting token refresh...")
            val refreshResult = authRepositoryProvider.get().attemptSilentRefresh()

            if (refreshResult.isSuccess) {
                Log.i("SafeApiCall", "Token refresh successful. Retrying original call to $endpointName.")
                // Reîncercăm apelul original o singură dată
                try {
                    Result.success(apiCall())
                } catch (e2: Exception) {
                    Log.e("SafeApiCall", "Call to $endpointName failed even after token refresh.", e2)
                    Result.failure(e2)
                }
            } else {
                Log.e("SafeApiCall", "Token refresh failed. Session is invalid.")
                Result.failure(Exception("Your session has expired. Please login again."))
            }
        } else {
            val errorMsg = "Server error fetching $endpointName: ${e.response.status.value} - ${e.message}"
            Log.e("SafeApiCall", errorMsg, e)
            Result.failure(Exception("Server error while loading $endpointName."))
        }
    } catch (e: IOException) {
        val errorMsg = "Network error fetching $endpointName: ${e.message}"
        Log.e("SafeApiCall", errorMsg, e)
        Result.failure(Exception("Network error. Please check your connection."))
    } catch (e: SerializationException) {
        val errorMsg = "Serialization error fetching $endpointName: ${e.message}"
        Log.e("SafeApiCall", errorMsg, e)
        Result.failure(Exception("Error parsing server response for $endpointName."))
    } catch (e: Exception) {
        val errorMsg = "Unexpected error fetching $endpointName: ${e.message}"
        Log.e("SafeApiCall", errorMsg, e)
        Result.failure(Exception("An unexpected error occurred."))
    }
}