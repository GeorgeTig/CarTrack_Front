package com.example.cartrack.feature.addmaintenance.data

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceItemDto(
    val typeId: Int,
    val name: String
)