package com.example.cartrack.feature.addvehicle.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VinDecodedResponseDto(
    @SerialName("seriesName") val seriesName: String,
    @SerialName("producer") val producer: String,
    @SerialName("vehicleModelInfo") val vehicleModelInfo: List<ModelDecodedDto> = emptyList()
)

@Serializable
data class ModelDecodedDto(
    @SerialName("year") val year: Int,
    @SerialName("modelId") val modelId: Int,
    @SerialName("engineInfo") val engineInfo: List<EngineInfoDto> = emptyList(),
    @SerialName("bodyInfo") val bodyInfo: List<BodyInfoDto> = emptyList()
)

@Serializable
data class EngineInfoDto(
    @SerialName("engineId") val engineId: Int,
    @SerialName("engineType") val engineType: String,
    @SerialName("driveType") val driveType: String,
    @SerialName("size") val size: Double,
    @SerialName("horsepower") val horsepower: Int,
    @SerialName("transmission") val transmission: String,
)

@Serializable
data class BodyInfoDto(
    @SerialName("bodyId") val bodyId: Int,
    @SerialName("bodyType") val bodyType: String,
    @SerialName("doorNumber") val doorNumber: Int,
    @SerialName("seatNumber") val seatNumber: Int,
)