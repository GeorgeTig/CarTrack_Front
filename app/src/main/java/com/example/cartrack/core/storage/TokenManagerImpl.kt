package com.example.cartrack.core.storage

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManagerImpl(
    private val context: Context
) : TokenManager {

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("jwt_access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("jwt_refresh_token")
        const val TAG = "TokenManagerImpl"
    }

    // Metodă internă pentru ștergere, fără loguri repetitive dacă e apelată din saveTokens
    private suspend fun internalDeleteTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        Log.d(TAG, "Attempting to save tokens. Clearing old tokens first.")
        // Șterge token-urile vechi înainte de a salva cele noi
        // Deși .edit { prefs[KEY] = value } ar trebui să suprascrie, facem explicit pentru siguranță.
        // Nota: DataStore.edit este atomic, deci nu e nevoie de delete separat dacă doar suprascrii.
        // Dar pentru a testa ipoteza ta, o putem face așa:
        // internalDeleteTokens() // Comentează/Decomentează asta pentru a testa diferența.
        // O suprascriere directă e de obicei suficientă și mai eficientă.

        Log.d(TAG, "Saving new tokens to DataStore...")
        Log.d(TAG, "New Access Token to save: ...${tokens.accessToken.takeLast(10)}")
        Log.d(TAG, "New Refresh Token to save: ...${tokens.refreshToken.takeLast(10)}")
        try {
            context.dataStore.edit { preferences ->
                // Suprascriem direct. DataStore gestionează asta.
                preferences[ACCESS_TOKEN_KEY] = tokens.accessToken
                preferences[REFRESH_TOKEN_KEY] = tokens.refreshToken
                Log.d(TAG, "Preferences edited. AT and RT set/overwritten in preferences object.")
            }
            Log.d(TAG, "Access and Refresh Tokens save/overwrite attempt to DataStore finished.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving/overwriting tokens to DataStore: ${e.message}", e)
        }
    }

    override val accessTokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading access token preferences: ${exception.localizedMessage}", exception)
                emit(emptyPreferences())
            } else {
                Log.e(TAG, "Unexpected error reading access token flow", exception)
                throw exception
            }
        }
        .map { preferences ->
            val token = preferences[ACCESS_TOKEN_KEY]
            Log.d(TAG, "accessTokenFlow emitted: ...${token?.takeLast(10)}")
            token
        }

    override val refreshTokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading refresh token preferences: ${exception.localizedMessage}", exception)
                emit(emptyPreferences())
            } else {
                Log.e(TAG, "Unexpected error reading refresh token flow", exception)
                throw exception
            }
        }
        .map { preferences ->
            val token = preferences[REFRESH_TOKEN_KEY]
            Log.d(TAG, "refreshTokenFlow emitted: ...${token?.takeLast(10)}")
            token
        }

    override suspend fun getTokens(): AuthTokens? {
        val accessToken = accessTokenFlow.firstOrNull()
        val refreshToken = refreshTokenFlow.firstOrNull()
        Log.d(TAG, "getTokens called. AT: ...${accessToken?.takeLast(10)}, RT: ...${refreshToken?.takeLast(10)}")

        return if (accessToken != null && refreshToken != null) {
            AuthTokens(accessToken, refreshToken)
        } else {
            null
        }
    }

    override suspend fun deleteTokens() {
        Log.d(TAG, "Explicitly deleting tokens from DataStore...")
        try {
            internalDeleteTokens() // Folosește metoda internă
            Log.d(TAG, "Access and Refresh Tokens deletion attempt from DataStore finished.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting tokens from DataStore: ${e.message}", e)
        }
    }
}