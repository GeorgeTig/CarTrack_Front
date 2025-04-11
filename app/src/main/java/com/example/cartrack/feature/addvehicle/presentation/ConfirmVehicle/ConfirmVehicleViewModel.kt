package com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
// ... other imports ...
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class ConfirmVehicleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // Inject repositories for SAVING later
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmVehicleUiState())
    val uiState: StateFlow<ConfirmVehicleUiState> = _uiState.asStateFlow()

    private var fullDecodedResults: List<VinDecodedResponseDto> = emptyList()

    init {
        processInitialResults(savedStateHandle)
    }

    private fun processInitialResults(savedStateHandle: SavedStateHandle) {
        // ... (same as before: get argument, decode, check empty, store in fullDecodedResults) ...
        val resultsJsonEncoded = savedStateHandle.get<String>(Routes.ARG_RESULTS_JSON) ?: run { /* set error */ return }
        try {
            val resultsJsonDecoded = URLDecoder.decode(resultsJsonEncoded, "UTF-8")
            fullDecodedResults = Json.decodeFromString(resultsJsonDecoded)
            if (fullDecodedResults.isEmpty()) { /* set error */ return }
            _uiState.update { it.copy(
                allDecodedOptions = fullDecodedResults,
                availableTopLevelOptions = fullDecodedResults
            )}
            analyzeAndSetInitialState()
        } catch (e: Exception) { /* set error */ }
    }

    private fun analyzeAndSetInitialState() {
        val results = fullDecodedResults
        if (results.isEmpty()) return

        val needsTopLevel = results.size > 1
        val initialTopLevelIndex = if (!needsTopLevel) 0 else null

        _uiState.update { it.copy(
            needsTopLevelSelection = needsTopLevel,
            selectedTopLevelIndex = initialTopLevelIndex
        )}

        if (initialTopLevelIndex != null) {
            processTopLevelSelection(initialTopLevelIndex)
        } else {
            resetSelectionsBelow(SelectionLevel.TOP_LEVEL)
        }
    }

    // --- Selection Handlers ---

    fun selectTopLevelOption(index: Int) {
        if (index < 0 || index >= fullDecodedResults.size) return
        _uiState.update { it.copy(selectedTopLevelIndex = index) }
        processTopLevelSelection(index)
    }

    private fun processTopLevelSelection(selectedIndex: Int) {
        val selectedResult = fullDecodedResults.getOrNull(selectedIndex) ?: return
        val allModels = selectedResult.vehicleModelInfo
        val uniqueYears = allModels.mapNotNull { it.year }.distinct().sortedDescending()
        val needsYear = uniqueYears.size > 1
        val initialYear = if (!needsYear && uniqueYears.isNotEmpty()) uniqueYears.first() else null

        _uiState.update { it.copy(
            availableYears = uniqueYears,
            needsYearSelection = needsYear,
            selectedYear = initialYear
        )}

        if (initialYear != null) {
            processYearSelection(initialYear)
        } else {
            resetSelectionsBelow(SelectionLevel.YEAR)
        }
    }

    fun selectYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        processYearSelection(year)
    }

    // ** NEW FLOW STARTS HERE **
    // After year is selected, determine available ENGINES across all models for that year
    private fun processYearSelection(selectedYear: Int) {
        val selectedTopLevelIndex = _uiState.value.selectedTopLevelIndex ?: return
        val selectedTopLevelData = fullDecodedResults.getOrNull(selectedTopLevelIndex) ?: return
        val modelsForYear = selectedTopLevelData.vehicleModelInfo.filter { it.year == selectedYear }

        // Flatten all engines from all models of the selected year, get unique ones by ID
        val availableEngines = modelsForYear
            .flatMap { it.engineInfo }
            .distinctBy { it.engineId } // Get unique engines based on ID

        val needsEngine = availableEngines.size > 1
        val initialEngineId = if (!needsEngine && availableEngines.isNotEmpty()) availableEngines.first().engineId else null

        _uiState.update {
            it.copy(
                // Reset downstream selections first
                selectedBodyId = null,
                selectedModelId = null,
                availableBodies = emptyList(),
                availableModels = emptyList(),
                needsBodySelection = true, // Assume needed until engine selected
                needsModelSelection = true, // Assume needed until engine/body selected
                isSelectionComplete = false, // Reset completeness

                // Set engine options
                availableEngines = availableEngines,
                needsEngineSelection = needsEngine,
                selectedEngineId = initialEngineId // Auto-select if unique
            )
        }

        // If engine was unique, proceed to process available bodies
        if (initialEngineId != null) {
            processEngineSelection(initialEngineId)
        }
    }


    // After engine is selected, determine available BODIES from models containing that engine
    fun selectEngine(engineId: Int) {
        _uiState.update { it.copy(selectedEngineId = engineId) }
        processEngineSelection(engineId)
    }

    private fun processEngineSelection(selectedEngineId: Int) {
        val selectedTopLevelIndex = _uiState.value.selectedTopLevelIndex ?: return
        val selectedTopLevelData = fullDecodedResults.getOrNull(selectedTopLevelIndex) ?: return
        val selectedYear = _uiState.value.selectedYear ?: return

        // Find models for the selected year that CONTAIN the selected engine
        val modelsWithEngine = selectedTopLevelData.vehicleModelInfo
            .filter { it.year == selectedYear && it.engineInfo.any { engine -> engine.engineId == selectedEngineId } }

        // Flatten all bodies from those models, get unique ones by ID
        val availableBodies = modelsWithEngine
            .flatMap { it.bodyInfo }
            .distinctBy { it.bodyId }

        val needsBody = availableBodies.size > 1
        val initialBodyId = if (!needsBody && availableBodies.isNotEmpty()) availableBodies.first().bodyId else null

        _uiState.update {
            it.copy(
                // Reset downstream selections
                selectedModelId = null,
                availableModels = emptyList(),
                needsModelSelection = true, // Assume needed until body selected
                isSelectionComplete = false,

                // Set body options
                availableBodies = availableBodies,
                needsBodySelection = needsBody,
                selectedBodyId = initialBodyId // Auto-select if unique
            )
        }

        // If body was unique, proceed to check for model ambiguity
        if (initialBodyId != null) {
            processBodySelection(initialBodyId)
        }
    }


    // After body is selected, determine the final MODEL(s) matching all criteria
    fun selectBody(bodyId: Int) {
        _uiState.update { it.copy(selectedBodyId = bodyId) }
        processBodySelection(bodyId)
    }

    private fun processBodySelection(selectedBodyId: Int) {
        val selectedTopLevelIndex = _uiState.value.selectedTopLevelIndex ?: return
        val selectedTopLevelData = fullDecodedResults.getOrNull(selectedTopLevelIndex) ?: return
        val selectedYear = _uiState.value.selectedYear ?: return
        val selectedEngineId = _uiState.value.selectedEngineId ?: return

        // Find models matching Year, selected Engine ID, AND selected Body ID
        val finalModels = selectedTopLevelData.vehicleModelInfo
            .filter { model ->
                model.year == selectedYear &&
                        model.engineInfo.any { engine -> engine.engineId == selectedEngineId } &&
                        model.bodyInfo.any { body -> body.bodyId == selectedBodyId }
            }

        val needsModel = finalModels.size > 1
        // If only one model remains, auto-select it
        val finalModelId = if (!needsModel && finalModels.isNotEmpty()) finalModels.first().modelId else null
        val selectionComplete = finalModelId != null // Selection is complete only if model is now determined

        _uiState.update {
            it.copy(
                availableModels = finalModels, // Models matching Engine+Body
                needsModelSelection = needsModel,
                selectedModelId = finalModelId,
                isSelectionComplete = selectionComplete
            )
        }
        // If needsModel is true, UI should show model selection section
    }

    // Called only if needsModelSelection is true and user picks a model
    fun selectModel(modelId: Int) {
        _uiState.update {
            it.copy(
                selectedModelId = modelId,
                isSelectionComplete = true // Selecting the final model makes it complete
            )
        }
    }


    // Helper to reset selections when a higher-level choice changes
    // Adjust levels according to the new flow
    private fun resetSelectionsBelow(level: SelectionLevel) {
        _uiState.update {
            it.copy(
                isSelectionComplete = false,
                // Reset Year and everything below if TopLevel changes
                selectedYear = if (level < SelectionLevel.YEAR) null else it.selectedYear,
                needsYearSelection = if (level <= SelectionLevel.TOP_LEVEL) true else it.needsYearSelection,
                availableYears = if (level < SelectionLevel.YEAR) emptyList() else it.availableYears,

                // Reset Engine and below if Year changes (or TopLevel)
                selectedEngineId = if (level < SelectionLevel.ENGINE) null else it.selectedEngineId,
                needsEngineSelection = if (level <= SelectionLevel.YEAR) true else it.needsEngineSelection,
                availableEngines = if (level < SelectionLevel.ENGINE) emptyList() else it.availableEngines,

                // Reset Body and below if Engine changes (or Year, TopLevel)
                selectedBodyId = if (level < SelectionLevel.BODY) null else it.selectedBodyId,
                needsBodySelection = if (level <= SelectionLevel.ENGINE) true else it.needsBodySelection,
                availableBodies = if (level < SelectionLevel.BODY) emptyList() else it.availableBodies,

                // Reset Model if Body changes (or Engine, Year, TopLevel)
                selectedModelId = if (level < SelectionLevel.MODEL) null else it.selectedModelId,
                needsModelSelection = if (level <= SelectionLevel.BODY) true else it.needsModelSelection,
                availableModels = if (level < SelectionLevel.MODEL) emptyList() else it.availableModels
            )
        }
    }

    // Adjusted Enum for new flow
    private enum class SelectionLevel { TOP_LEVEL, YEAR, ENGINE, BODY, MODEL }

    // --- Error Handling ---
    fun errorShown() { _uiState.update { it.copy(error = null) } }

    // --- TODO: Add Save Logic ---
    // fun confirmAndSaveVehicle() { ... gather IDs from state ... call repo ... }
}