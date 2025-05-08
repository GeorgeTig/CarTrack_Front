package com.example.cartrack.feature.addvehicle.presentation

import com.example.cartrack.feature.addvehicle.data.model.* // Import all necessary models

// Unified UI State for the multi-step Add Vehicle process
data class AddVehicleUiState(
    // Step tracking
    val currentStep: AddVehicleStep = AddVehicleStep.VIN,

    // VIN Input State
    val vinInput: String = "",
    val vinValidationError: String? = null,

    // Loading/Error/Save State
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaveSuccess: Boolean = false,

    // Decoded Options (original API response)
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),

    // Selection State
    val selectedProducer: String? = null,
    val selectedSeriesDto: VinDecodedResponseDto? = null,
    val determinedYear: Int? = null,
    val confirmedEngine: EngineInfoDto? = null,
    val confirmedBody: BodyInfoDto? = null,
    val selectedModelId: Int? = null,

    // Available Options for Dropdowns
    val availableProducers: List<String> = emptyList(),
    val availableSeries: List<VinDecodedResponseDto> = emptyList(),
    val availableEngines: List<EngineInfoDto> = emptyList(),
    val availableBodies: List<BodyInfoDto> = emptyList(),
    val availableModels: List<ModelDecodedDto> = emptyList(),

    // --- Selection Requirement Flags ---
    val needsProducerSelection: Boolean = false,
    val needsSeriesSelection: Boolean = false,
    val needsEngineConfirmation: Boolean = false,
    val needsBodyConfirmation: Boolean = false,
    val needsModelSelection: Boolean = false,

    // --- Mileage State ---
    val mileageInput: String = "",
    val mileageValidationError: String? = null,

    // --- Derived flags for buttons ---
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false
)


//Helper Display Functions
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

// Helper to get display string for confirmed selections
fun AddVehicleUiState.getFinalConfirmedModelDetailsForDisplay(): String {
    return buildString {
        selectedProducer?.let { append("$it ") }
        selectedSeriesDto?.seriesName?.let { append("$it ") }
        determinedYear?.let { append("($it) ") }
    }.trim().replace("  ", " ")
}