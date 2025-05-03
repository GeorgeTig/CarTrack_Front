package com.example.cartrack.feature.addvehicle.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleSaveRequestDto(
    @SerialName("clientId") val clientId: Int,
    @SerialName("modelId") val modelId: Int, // Matches the C# DTO
    @SerialName("vin") val vin: String,
    @SerialName("mileage") val mileage: Double // Matches the C# DTO
)
