package com.example.cartrack.core.data.model.vehicle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    val id: Int,
    val vin: String,
    val series: String,
    val year: Int,
    val producer: String,
    val imageUrl: String? = null
)

@Serializable
data class VehicleListResponseDto(
    @SerialName("result")
    val vehicles: List<VehicleResponseDto>
)

@Serializable
data class VehicleInfoResponseDto(
    val mileage: Double,
    val travelDistanceAVG: Double = 0.0,
    val lastUpdate: String = ""
)

@Serializable
data class DailyUsageDto(
    val dayLabel: String,
    val distance: Double
)

@Serializable
data class VehicleModelResponseDto(
    val id: Int,
    val modelName: String,
    val series: String,
    val year: Int,
    val fuelTankCapacity: Long,
    val consumption: Long
)

@Serializable
data class VehicleEngineResponseDto(
    val id: Int,
    val engineType: String,
    val fuelType: String,
    val cylinders: String,
    val size: Double,
    val horsePower: Int,
    val torqueFtLbs: Int,
    val driveType: String,
    val transmission: String
)

@Serializable
data class VehicleBodyResponseDto(
    val id: Int,
    val bodyType: String,
    val doorNumber: Int,
    val seatNumber: Int,
)

@Serializable
data class VehicleSaveRequestDto(
    @SerialName("clientId") val clientId: Int,
    @SerialName("modelId") val modelId: Int,
    @SerialName("vin") val vin: String,
    @SerialName("mileage") val mileage: Double
)