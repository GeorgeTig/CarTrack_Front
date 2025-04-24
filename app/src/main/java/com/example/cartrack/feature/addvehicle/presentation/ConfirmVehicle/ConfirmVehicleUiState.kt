package com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle

import com.example.cartrack.feature.addvehicle.data.model.*

// Data class holding the state for the Confirm Vehicle screen
data class ConfirmVehicleUiState(
    // General UI states
    val isLoading: Boolean = false, // True when performing async operations (like saving)
    val error: String? = null,      // Holds user-friendly error messages
    val isSaveSuccess: Boolean = false, // <<< NEW: Flag for successful save navigation

    // Initial data from VIN decode
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),

    // --- Selection State ---
    // Stores the currently selected values/DTOs at each level
    val selectedProducer: String? = null,             // Selected manufacturer name
    val selectedSeriesDto: VinDecodedResponseDto? = null, // Full DTO for the selected series (contains models)
    val selectedYear: Int? = null,                    // Selected model year

    // Engine Selection: Temporary selection while dropdown is open, and the final confirmed choice
    val temporarilySelectedEngine: EngineInfoDto? = null, // Engine selected in dropdown *before* confirming
    val confirmedEngine: EngineInfoDto? = null,          // Engine confirmed by user or auto-selected
    val isEditingEngine: Boolean = false,             // True if user clicked "Edit" on confirmed engine

    // Body Selection: Temporary selection while dropdown is open, and the final confirmed choice
    val temporarilySelectedBody: BodyInfoDto? = null,     // Body selected in dropdown *before* confirming
    val confirmedBody: BodyInfoDto? = null,              // Body confirmed by user or auto-selected
    val isEditingBody: Boolean = false,               // True if user clicked "Edit" on confirmed body

    // Model Selection: Final model ID if disambiguation was needed
    val selectedModelId: Int? = null,

    // --- Interaction Flags (Derived in ViewModel) ---
    // Indicate whether user input is required at each step
    val needsProducerSelection: Boolean = false, // True if multiple producers exist
    val needsSeriesSelection: Boolean = false,   // True if multiple series for selected producer
    val needsYearSelection: Boolean = false,     // True if multiple years for selected series
    val needsEngineConfirmation: Boolean = false, // True if multiple engines require manual confirmation
    val needsBodyConfirmation: Boolean = false,   // True if multiple bodies require manual confirmation
    val needsModelSelection: Boolean = false,     // True if multiple models match final specs

    // Flag indicating if all necessary selections are made and the vehicle can be saved
    val isSelectionComplete: Boolean = false,

    // --- Available Options (Derived in ViewModel for Dropdowns) ---
    val availableProducers: List<String> = emptyList(),                  // Unique producer names
    val availableSeries: List<VinDecodedResponseDto> = emptyList(),      // Series matching selected producer
    val availableYears: List<Int> = emptyList(),                     // Years matching selected series
    val availableEngines: List<EngineInfoDto> = emptyList(),           // Engines matching selected year
    val availableBodies: List<BodyInfoDto> = emptyList(),              // Bodies matching selected engine
    val availableModels: List<ModelDecodedDto> = emptyList(),          // Models matching selected body (if needed)

    // --- UI Control Flags (Derived in ViewModel) ---
    // Determine whether to show dropdowns or confirmed info for Engine/Body
    val showEngineDropdown: Boolean = false, // True if engine dropdown should be visible
    val showBodyDropdown: Boolean = false,   // True if body dropdown should be visible
)

// --- Helper Display Functions ---

/** Provides a user-friendly string representation of an EngineInfoDto */
fun EngineInfoDto?.displayString(): String {
    if (this == null) return "Not Selected" // Or "" depending on preference
    return buildString {
        // Combine key specs for display
        engineType?.let { append("$it ") }
        size?.let { append("${it}L ") }
        horsepower?.let { append("${it}hp ") }
        transmission?.let { append("$it ") }
        driveType?.let { append("($it) ") }
    }.trim() // Remove trailing space
}

/** Provides a user-friendly string representation of a BodyInfoDto */
fun BodyInfoDto?.displayString(): String {
    if (this == null) return "Not Selected" // Or ""
    return buildString {
        bodyType?.let { append("$it ") }
        doorNumber?.let { append("${it}-Door ") }
        seatNumber?.let { append("${it}-Seat ") }
    }.trim()
}

/**
 * Helper to get the final selected Model DTO based on the selectedModelId.
 * Searches within the filtered availableModels first.
 */
fun ConfirmVehicleUiState.getSelectedModelDto(): ModelDecodedDto? {
    val modelId = selectedModelId ?: return null
    // Prefer finding within the specifically filtered models (if available)
    return availableModels.find { it.modelId == modelId }
    // Fallback: Search the selected series' models if availableModels wasn't populated/needed
        ?: selectedSeriesDto?.vehicleModelInfo?.find { it.modelId == modelId }
}