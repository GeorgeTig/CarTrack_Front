package com.example.cartrack.core.storage

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey // Use intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Use a different file name for this DataStore instance
private val Context.vehicleCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "vehicle_cache_prefs")

class VehicleManagerImpl(
    private val context: Context
) : VehicleManager {

    private companion object {
        // Use an Int key
        val LAST_VEHICLE_ID_KEY = intPreferencesKey("last_vehicle_id")
        const val TAG = "VehicleCacheManager"
    }

    override suspend fun saveLastVehicleId(id: Int) {
        context.vehicleCacheDataStore.edit { preferences ->
            preferences[LAST_VEHICLE_ID_KEY] = id
        }
        Log.d(TAG, "Saved last vehicle ID: $id")
    }

    override val lastVehicleIdFlow: Flow<Int?> = context.vehicleCacheDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading vehicle cache preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_VEHICLE_ID_KEY] // Directly get Int?
        }

    override suspend fun deleteLastVehicleId() {
        context.vehicleCacheDataStore.edit { preferences ->
            preferences.remove(LAST_VEHICLE_ID_KEY)
        }
        Log.d(TAG, "Deleted last vehicle ID from cache.")
    }
}