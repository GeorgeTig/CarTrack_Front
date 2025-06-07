package com.example.cartrack.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_tokens")

class TokenManagerImpl(private val context: Context) : TokenManager {

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("jwt_access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("jwt_refresh_token")
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        context.tokenDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = tokens.accessToken
            preferences[REFRESH_TOKEN_KEY] = tokens.refreshToken
        }
    }

    override suspend fun getTokens(): AuthTokens? {
        val prefs = context.tokenDataStore.data.firstOrNull() ?: return null
        val accessToken = prefs[ACCESS_TOKEN_KEY]
        val refreshToken = prefs[REFRESH_TOKEN_KEY]
        return if (accessToken != null && refreshToken != null) {
            AuthTokens(accessToken, refreshToken)
        } else {
            null
        }
    }

    override suspend fun deleteTokens() {
        context.tokenDataStore.edit { it.clear() }
    }

    override val accessTokenFlow: Flow<String?> = context.tokenDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[ACCESS_TOKEN_KEY] }

    override val refreshTokenFlow: Flow<String?> = context.tokenDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[REFRESH_TOKEN_KEY] }
}