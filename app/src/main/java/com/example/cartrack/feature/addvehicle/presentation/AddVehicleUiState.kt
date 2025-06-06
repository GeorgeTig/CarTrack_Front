package com.example.cartrack.feature.addvehicle.presentation

import com.example.cartrack.feature.addvehicle.data.model.*

enum class AddVehicleStep {
    VIN,
    SERIES_YEAR,
    ENGINE_DETAILS,
    BODY_DETAILS,
    VEHICLE_INFO,
    CONFIRM
}

data class AddVehicleUiState(
    // Step tracking
    val currentStep: AddVehicleStep = AddVehicleStep.VIN,
    val totalSteps: Int = AddVehicleStep.entries.size,

    // VIN Input & Decoding State
    val vinInput: String = "",
    val vinValidationError: String? = null,
    val isLoadingVinDetails: Boolean = false, // Specific pentru decodare VIN
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),

    // Stări pentru selecțiile utilizatorului
    val availableSeriesAndYears: List<Pair<String, Int>> = emptyList(),
    val selectedSeriesName: String? = null,
    val selectedYear: Int? = null,

    val availableEngineSizes: List<Double> = emptyList(),
    val selectedEngineSize: Double? = null,
    val availableEngineTypes: List<String> = emptyList(),
    val selectedEngineType: String? = null,
    val availableTransmissions: List<String> = emptyList(),
    val selectedTransmission: String? = null,
    val availableDriveTypes: List<String> = emptyList(),
    val selectedDriveType: String? = null,
    val confirmedEngineId: Int? = null,
    val availableSeatNumbers: List<Int> = emptyList(),
    val selectedSeatNumber: Int? = null,

    val availableBodyTypes: List<String> = emptyList(),
    val selectedBodyType: String? = null,
    val availableDoorNumbers: List<Int> = emptyList(),
    val selectedDoorNumber: Int? = null,
    val confirmedBodyId: Int? = null,

    val mileageInput: String = "",
    val mileageValidationError: String? = null,
    val determinedModelId: Int? = null,

    // Stări generale de UI
    val isLoadingNextStep: Boolean = false, // Spinner pe butonul "Next"
    val isSaving: Boolean = false,         // Spinner pentru operațiunea de salvare
    val error: String? = null,             // Mesaj de eroare general
    val isSaveSuccess: Boolean = false,

    // Control butoane
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false
    // Nu mai avem nevoie de clientId aici
)

// Funcțiile helper displayString și getFinalConfirmedModelDetailsForDisplay rămân la fel
fun EngineInfoDto?.displayString(): String {
    if (this == null) return "N/A"
    return buildString {
        append("$engineType ")
        append("${size}L ")
        append("${horsepower}hp ")
        append("$transmission ")
        append("($driveType) ")
    }.trim().ifEmpty { "Details Unavailable" }
}

fun BodyInfoDto?.displayString(): String {
    if (this == null) return "N/A"
    return buildString {
        append("$bodyType ")
        append("${doorNumber}-Door ")
        append("${seatNumber}-Seat ")
    }.trim().ifEmpty { "Details Unavailable" }
}

fun AddVehicleUiState.getFinalConfirmedModelDetailsForDisplay(): String {
    val producer = allDecodedOptions.find { it.seriesName == selectedSeriesName && it.vehicleModelInfo.any { vm -> vm.year == selectedYear } }?.producer
    return buildString {
        producer?.let { append("$it ") }
        selectedSeriesName?.let { append("$it ") }
        selectedYear?.let { append("($it) ") }
    }.trim().replace("  ", " ")
}