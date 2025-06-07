package com.example.cartrack.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.vehicleDataStore: DataStore<Preferences> by preferencesDataStore(name = "vehicle_cache")

class VehicleManagerImpl(private val context: Context) : VehicleManager {

    private companion object {
        val LAST_VEHICLE_ID_KEY = intPreferencesKey("last_vehicle_id_key")
    }

    override suspend fun saveLastVehicleId(id: Int) {
        context.vehicleDataStore.edit { it[LAST_VEHICLE_ID_KEY] = id }
    }

    override suspend fun deleteLastVehicleId() {
        context.vehicleDataStore.edit { it.remove(LAST_VEHICLE_ID_KEY) }
    }

    override val lastVehicleIdFlow: Flow<Int?> = context.vehicleDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[LAST_VEHICLE_ID_KEY] }
}