package com.example.cartrack.core.storage

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Use a different file name for this DataStore instance
private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserManagerImpl(
    private val context: Context
) : UserManager {

    private companion object {
        val HAS_NEW_NOTIFICATIONS_KEY = booleanPreferencesKey("has_new_notifications")
        val CLIENT_ID_KEY = intPreferencesKey("client_id_user_manager") // Unique key name
        const val TAG = "UserManager"
    }

    override val hasNewNotificationsFlow: Flow<Boolean> = context.userPrefsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading 'hasNewNotifications' from preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[HAS_NEW_NOTIFICATIONS_KEY] ?: false // Default to false if not set
        }

    override suspend fun setHasNewNotifications(hasNew: Boolean) {
        context.userPrefsDataStore.edit { preferences ->
            preferences[HAS_NEW_NOTIFICATIONS_KEY] = hasNew
        }
        Log.d(TAG, "Set 'hasNewNotifications' to: $hasNew")
    }

    override suspend fun saveClientId(clientId: Int) {
        context.userPrefsDataStore.edit { preferences ->
            preferences[CLIENT_ID_KEY] = clientId
        }
        Log.d(TAG, "Saved client ID: $clientId")
    }

    override val clientIdFlow: Flow<Int?> = context.userPrefsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading client ID from preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[CLIENT_ID_KEY]
        }


    override suspend fun clearUserData() {
        context.userPrefsDataStore.edit { preferences ->
            preferences.remove(HAS_NEW_NOTIFICATIONS_KEY)
            preferences.remove(CLIENT_ID_KEY)
            // Add other keys to remove if you add more user-specific data
        }
        Log.d(TAG, "Cleared user-specific data from UserManager.")
    }
}