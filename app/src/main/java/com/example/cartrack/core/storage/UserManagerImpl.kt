package com.example.cartrack.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data")

class UserManagerImpl(private val context: Context) : UserManager {

    private companion object {
        val CLIENT_ID_KEY = intPreferencesKey("client_id_key")
        val HAS_NEW_NOTIFICATIONS_KEY = booleanPreferencesKey("has_new_notifications_key")
    }

    override suspend fun saveClientId(clientId: Int) {
        context.userDataStore.edit { it[CLIENT_ID_KEY] = clientId }
    }

    override suspend fun setHasNewNotifications(hasNew: Boolean) {
        context.userDataStore.edit { it[HAS_NEW_NOTIFICATIONS_KEY] = hasNew }
    }

    override suspend fun clearUserData() {
        context.userDataStore.edit { it.clear() }
    }

    override val clientIdFlow: Flow<Int?> = context.userDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[CLIENT_ID_KEY] }

    override val hasNewNotificationsFlow: Flow<Boolean> = context.userDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[HAS_NEW_NOTIFICATIONS_KEY] ?: false }
}