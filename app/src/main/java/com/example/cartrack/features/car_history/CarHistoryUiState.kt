package com.example.cartrack.features.car_history

import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto

data class CarHistoryState(
    val isLoading: Boolean = true,
    val vehicleId: Int? = null,
    val vehicleName: String = "",
    val groupedEvents: List<Pair<String, List<MaintenanceLogResponseDto>>> = emptyList(),
    val error: String? = null
)