package com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle

import com.example.cartrack.feature.addvehicle.data.model.*

data class ConfirmVehicleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(), // Raw results

    // --- Selection State (Storing IDs) ---
    val selectedTopLevelIndex: Int? = null,
    val selectedYear: Int? = null,
    // Engine and Body are selected BEFORE Model now
    val selectedEngineId: Int? = null,
    val selectedBodyId: Int? = null,
    // Model is selected LAST (or implicitly determined)
    val selectedModelId: Int? = null,

    // --- Ambiguity Flags ---
    val needsTopLevelSelection: Boolean = false,
    val needsYearSelection: Boolean = false,
    // Order changed: Engine/Body needs determined after Year
    val needsEngineSelection: Boolean = false,
    val needsBodySelection: Boolean = false,
    // Model selection only needed if Engine+Body don't yield unique Model
    val needsModelSelection: Boolean = false, // <<< ADDED

    // Derived property: Checks if all required selections have been made
    val isSelectionComplete: Boolean = false,

    // --- Available Options (derived for UI based on current selections) ---
    val availableTopLevelOptions: List<VinDecodedResponseDto> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    // Order changed: Engines available after Year selected
    val availableEngines: List<EngineInfoDto> = emptyList(), // <<< CHANGED: All unique engines for the Year
    // Bodies available after Engine selected (and potentially filtered by models containing that engine)
    val availableBodies: List<BodyInfoDto> = emptyList(), // <<< CHANGED: All unique bodies for the Engine
    // Models available ONLY if Engine+Body choice is ambiguous
    val availableModels: List<ModelDecodedDto> = emptyList(), // <<< CHANGED: Models matching final Engine+Body choice
)

// Helper extension function to get the currently selected Model DTO if determined
// Useful for enabling/disabling save button or final confirmation display
fun ConfirmVehicleUiState.getSelectedModelDto(): ModelDecodedDto? {
    if (selectedModelId == null) return null
    // Find the model within the availableModels list (which should be filtered correctly)
    // Or search through allDecodedOptions if availableModels isn't populated yet? - Needs care
    return availableModels.find { it.modelId == selectedModelId }
        ?: allDecodedOptions.getOrNull(selectedTopLevelIndex ?: -1)
            ?.vehicleModelInfo?.find { it.year == selectedYear && it.modelId == selectedModelId }

}