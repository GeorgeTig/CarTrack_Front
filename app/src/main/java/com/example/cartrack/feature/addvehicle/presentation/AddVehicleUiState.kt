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
    val isLoading: Boolean = false, // True during VIN decode or final save
    val error: String? = null,      // General errors (network, save failures, logic errors)
    val isSaveSuccess: Boolean = false, // Flag to trigger navigation on successful save

    // Decoded Options (from original API response)
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),

    // --- Selection State ---
    val selectedProducer: String? = null,
    val selectedSeriesDto: VinDecodedResponseDto? = null,
    val determinedYear: Int? = null, // Store the implicitly determined year after series selection
    val confirmedEngine: EngineInfoDto? = null, // Final selected/confirmed engine
    val confirmedBody: BodyInfoDto? = null,     // Final selected/confirmed body
    val selectedModelId: Int? = null,           // Final specific model ID if disambiguation was needed

    // --- Available Options for Dropdowns (Dynamically filtered) ---
    val availableProducers: List<String> = emptyList(),
    val availableSeries: List<VinDecodedResponseDto> = emptyList(),
    // availableYears removed
    val availableEngines: List<EngineInfoDto> = emptyList(),
    val availableBodies: List<BodyInfoDto> = emptyList(),
    val availableModels: List<ModelDecodedDto> = emptyList(), // Only populated if final model selection needed

    // --- Selection Requirement Flags (True if user interaction needed) ---
    val needsProducerSelection: Boolean = false,
    val needsSeriesSelection: Boolean = false,
    // needsYearSelection removed
    val needsEngineConfirmation: Boolean = false, // True if >1 engine option exists
    val needsBodyConfirmation: Boolean = false,   // True if >1 body option exists
    val needsModelSelection: Boolean = false,     // True if >1 final model matches criteria

    // --- Mileage State ---
    val mileageInput: String = "",
    val mileageValidationError: String? = null,

    // --- Derived flags for enabling Next/Previous buttons ---
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false
)

// --- Helper Display Functions (Keep these accessible, e.g., in the same file or a utils file) ---
fun EngineInfoDto?.displayString(): String {
    if (this == null) return "N/A" // Use N/A for confirm screen clarity
    return buildString {
        engineType?.let { append("$it ") }
        size?.let { append("${it}L ") } // Use ?. for potentially null Double/Int
        horsepower?.let { append("${it}hp ") } // Use ?.
        transmission?.let { append("$it ") }
        driveType?.let { append("($it) ") }
    }.trim().ifEmpty { "Details Unavailable" } // Fallback if all fields were null
}

fun BodyInfoDto?.displayString(): String {
    if (this == null) return "N/A" // Use N/A
    return buildString {
        bodyType?.let { append("$it ") }
        doorNumber?.let { append("${it}-Door ") } // Use ?.
        seatNumber?.let { append("${it}-Seat ") } // Use ?.
    }.trim().ifEmpty { "Details Unavailable" } // Fallback
}

// Helper to get the final confirmed model details for display (Example)
fun AddVehicleUiState.getFinalConfirmedModelDetailsForDisplay(): String {
    val modelDto = selectedModelId?.let { id ->
        // Search within the final available models first
        availableModels.find { it.modelId == id }
        // Fallback to searching all models within the selected series
            ?: selectedSeriesDto?.vehicleModelInfo?.find { it.modelId == id }
    }

    // Build a descriptive string based on confirmed selections
    return buildString {
        selectedProducer?.let { append("$it ") }
        selectedSeriesDto?.seriesName?.let { append("$it ") }
        determinedYear?.let { append("($it) ") } // Use determinedYear
        // Add model-specific info if available and needed
        // modelDto?.someSpecificModelField?.let { append("...") }
    }.trim().replace("  ", " ")
}