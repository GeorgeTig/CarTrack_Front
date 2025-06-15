package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCacheRepository @Inject constructor() {

    private val _vehicles = MutableStateFlow<List<VehicleResponseDto>?>(null)
    val vehicles: StateFlow<List<VehicleResponseDto>?> = _vehicles

    fun setVehicles(vehicles: List<VehicleResponseDto>) {
        _vehicles.value = vehicles
    }

    fun clearCache() {
        _vehicles.value = null
    }
}