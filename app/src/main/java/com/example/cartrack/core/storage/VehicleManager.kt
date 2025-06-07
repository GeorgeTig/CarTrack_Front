package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

interface VehicleManager {
    suspend fun saveLastVehicleId(id: Int)
    suspend fun deleteLastVehicleId()

    val lastVehicleIdFlow: Flow<Int?>
}