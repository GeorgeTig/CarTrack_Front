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
    val isLoadingVinDetails: Boolean = false,
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),

    // --- Stări pentru selecții în cascadă (REVENIM LA ACEASTĂ STRUCTURĂ) ---
    val availableSeriesAndYears: List<Pair<String, Int>> = emptyList(),
    val selectedSeriesName: String? = null,
    val selectedYear: Int? = null,

    // Pentru Engine
    val availableEngineSizes: List<Double> = emptyList(),
    val selectedEngineSize: Double? = null,
    val availableEngineTypes: List<String> = emptyList(),
    val selectedEngineType: String? = null,
    val availableTransmissions: List<String> = emptyList(),
    val selectedTransmission: String? = null,
    val availableDriveTypes: List<String> = emptyList(),
    val selectedDriveType: String? = null,
    val confirmedEngineId: Int? = null,

    // Pentru Body
    val availableBodyTypes: List<String> = emptyList(),
    val selectedBodyType: String? = null,
    val availableDoorNumbers: List<Int> = emptyList(),
    val selectedDoorNumber: Int? = null,
    val availableSeatNumbers: List<Int> = emptyList(),
    val selectedSeatNumber: Int? = null,
    val confirmedBodyId: Int? = null,

    // Restul stărilor
    val mileageInput: String = "",
    val mileageValidationError: String? = null,
    val determinedModelId: Int? = null,

    // Stări generale de UI
    val isLoadingNextStep: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaveSuccess: Boolean = false,

    // Control butoane
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false,

    // Flag pentru a controla afișarea erorilor doar după ce s-a încercat trecerea la pasul următor
    val hasAttemptedNext: Boolean = false
)

// Funcțiile helper rămân la fel
fun AddVehicleUiState.getFinalConfirmedModelDetailsForDisplay(): String {
    val producer = allDecodedOptions.find { it.seriesName == selectedSeriesName && it.vehicleModelInfo.any { vm -> vm.year == selectedYear } }?.producer
    return buildString {
        producer?.let { append("$it ") }
        selectedSeriesName?.let { append("$it ") }
        selectedYear?.let { append("($it) ") }
    }.trim().replace("  ", " ")
}