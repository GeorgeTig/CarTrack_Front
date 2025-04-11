package com.example.cartrack.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
// No @Inject needed on constructor if provided via @Provides in a Module

// Define the DataStore instance using the preferencesDataStore delegate
// Needs to be at the top level of the file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

// This class ONLY handles DataStore interaction for the token
class TokenManagerImpl( // Constructor takes Context (provided by Hilt Module)
    private val context: Context
) : TokenManager { // IMPLEMENTS the TokenManager interface

    // Define a Preferences Key for the JWT token
    private companion object {
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    /**
     * Saves the JWT token to DataStore.
     */
    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
        println("Token saved.") // Add log for confirmation
    }

    /**
     * Retrieves the JWT token from DataStore as a Flow.
     */
    override val tokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException if it can't read the data
            if (exception is IOException) {
                System.err.println("Error reading preferences: ${exception.localizedMessage}")
                emit(emptyPreferences()) // Emit empty preferences on error
            } else {
                throw exception // Rethrow other exceptions
            }
        }
        .map { preferences ->
            val token = preferences[JWT_TOKEN_KEY]
            // println("Token flow emitting: ${token?.take(10)}...") // Log token presence
            token // Returns null if key doesn't exist
        }

    /**
     * Deletes the JWT token from DataStore.
     */
    override suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
        println("Token deleted.") // Add log for confirmation
    }
}