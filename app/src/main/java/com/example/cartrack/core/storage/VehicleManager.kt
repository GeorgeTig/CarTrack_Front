package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

/**
 * Manages caching the ID of the last viewed/relevant vehicle.
 */
interface VehicleManager {
    /**
     * Saves the ID of the last relevant vehicle.
     */
    suspend fun saveLastVehicleId(id: Int)

    /**
     * Retrieves the last saved vehicle ID as a Flow. Emits null if no ID is cached.
     */
    val lastVehicleIdFlow: Flow<Int?>

    /**
     * Deletes the cached vehicle ID.
     */
    suspend fun deleteLastVehicleId()
}
