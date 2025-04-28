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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")


class TokenManagerImpl(
    private val context: Context
) : TokenManager {

    /**
     * Key for storing the JWT token in DataStore.
     */
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
            if (exception is IOException) {
                System.err.println("Error reading preferences: ${exception.localizedMessage}")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val token = preferences[JWT_TOKEN_KEY]
            token
        }

    /**
     * Deletes the JWT token from DataStore.
     */
    override suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
        println("Token deleted.")
    }
}