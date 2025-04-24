package com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle

import android.util.Log // Optional: For debugging
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.navigation.Routes
// TODO: Import necessary repository for saving
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class ConfirmVehicleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
    // TODO: Inject SaveVehicleRepository here when ready
    // private val saveRepository: SaveVehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmVehicleUiState())
    val uiState: StateFlow<ConfirmVehicleUiState> = _uiState.asStateFlow()

    // Store the raw results received from navigation argument
    private var fullDecodedResults: List<VinDecodedResponseDto> = emptyList()

    init {
        processInitialResults(savedStateHandle)
    }

    // Decode the JSON argument passed via navigation
    private fun processInitialResults(savedStateHandle: SavedStateHandle) {
        val resultsJsonEncoded = savedStateHandle.get<String>(Routes.ARG_RESULTS_JSON)
        if (resultsJsonEncoded.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Error: Missing vehicle data.", isLoading = false) }
            return
        }
        try {
            val resultsJsonDecoded = URLDecoder.decode(resultsJsonEncoded, "UTF-8")
            fullDecodedResults = Json.decodeFromString(resultsJsonDecoded) // Let Kotlin infer type

            if (fullDecodedResults.isEmpty()) {
                _uiState.update { it.copy(error = "Error: No vehicle data found.", isLoading = false) }
                return
            }
            _uiState.update { it.copy(
                allDecodedOptions = fullDecodedResults,
                isLoading = false
            )}
            // Start the selection analysis process
            analyzeAndSetInitialState()
        } catch (e: Exception) {
            Log.e("ConfirmVM", "Error processing initial results", e)
            _uiState.update { it.copy(error = "Error processing vehicle data.", isLoading = false) }
        }
    }

    // Determine initial available producers and auto-select if only one
    private fun analyzeAndSetInitialState() {
        val results = fullDecodedResults
        if (results.isEmpty()) return

        val uniqueProducers = results.mapNotNull { it.producer }.distinct().sorted()
        val needsProducer = uniqueProducers.size > 1
        val initialProducer = if (!needsProducer && uniqueProducers.isNotEmpty()) uniqueProducers.first() else null

        _uiState.update { it.copy(
            availableProducers = uniqueProducers,
            selectedProducer = initialProducer,
            // Reset everything else
            availableSeries = emptyList(), selectedSeriesDto = null,
            availableYears = emptyList(), selectedYear = null,
            availableEngines = emptyList(), temporarilySelectedEngine = null, confirmedEngine = null, isEditingEngine = false,
            availableBodies = emptyList(), temporarilySelectedBody = null, confirmedBody = null, isEditingBody = false,
            availableModels = emptyList(), selectedModelId = null,
            isSelectionComplete = false, isSaveSuccess = false
        )}

        // If producer was unique, proceed to determine series options
        if (initialProducer != null) {
            processProducerSelection(initialProducer)
        }
        // Update flags based on initial state
        updateUiControlFlags()
    }

    // --- Selection Handlers ---

    /** Called when the user selects a producer from the dropdown. */
    fun selectProducer(producer: String) {
        // Reset everything below producer when selection changes
        resetSelectionsBelow(SelectionLevel.PRODUCER)
        _uiState.update { it.copy(selectedProducer = producer) }
        processProducerSelection(producer)
    }

    /** Filters series based on the selected producer and auto-selects if unique. */
    private fun processProducerSelection(selectedProducer: String) {
        val seriesForProducer = fullDecodedResults.filter { it.producer == selectedProducer }
        val needsSeries = seriesForProducer.size > 1
        val initialSeries = if (!needsSeries && seriesForProducer.isNotEmpty()) seriesForProducer.first() else null

        _uiState.update {
            it.copy(
                availableSeries = seriesForProducer,
                selectedSeriesDto = initialSeries,
                // Reset downstream state implicitly via resetSelectionsBelow in selectProducer/initial setup
            )
        }

        // If series was unique, proceed to determine year options
        if (initialSeries != null) {
            processSeriesSelection(initialSeries)
        }
        updateUiControlFlags()
    }

    /** Called when the user selects a series from the dropdown. */
    fun selectSeries(seriesDto: VinDecodedResponseDto) {
        resetSelectionsBelow(SelectionLevel.SERIES)
        _uiState.update { it.copy(selectedSeriesDto = seriesDto) }
        processSeriesSelection(seriesDto)
    }

    /** Filters years based on the selected series and auto-selects if unique. */
    private fun processSeriesSelection(selectedSeries: VinDecodedResponseDto) {
        val allModels = selectedSeries.vehicleModelInfo
        val uniqueYears = allModels.mapNotNull { it.year }.distinct().sortedDescending()
        val needsYear = uniqueYears.size > 1
        val initialYear = if (!needsYear && uniqueYears.isNotEmpty()) uniqueYears.first() else null

        _uiState.update {
            it.copy(
                availableYears = uniqueYears,
                selectedYear = initialYear,
                // Reset downstream state implicitly
            )
        }

        // If year was unique, proceed to determine engine options
        if (initialYear != null) {
            processYearSelection(initialYear)
        }
        updateUiControlFlags()
    }

    /** Called when the user selects a year from the dropdown. */
    fun selectYear(year: Int) {
        resetSelectionsBelow(SelectionLevel.YEAR)
        _uiState.update { it.copy(selectedYear = year) }
        processYearSelection(year)
    }

    /** Filters engines based on the selected year and auto-confirms if unique. */
    private fun processYearSelection(selectedYear: Int) {
        val selectedSeriesData = _uiState.value.selectedSeriesDto ?: return
        val modelsForYear = selectedSeriesData.vehicleModelInfo.filter { it.year == selectedYear }

        val availableEngines = modelsForYear
            .flatMap { it.engineInfo }
            .distinctBy { it.engineId }

        val autoSelectedEngine = if (availableEngines.size == 1) availableEngines.first() else null

        _uiState.update {
            it.copy(
                availableEngines = availableEngines,
                confirmedEngine = autoSelectedEngine, // Confirm directly if unique
                isEditingEngine = false, // Ensure not in editing mode if auto-confirmed
                temporarilySelectedEngine = null, // Clear temporary selection
                // Reset downstream state implicitly
            )
        }

        // If engine was auto-confirmed, proceed to determine body options
        if (autoSelectedEngine != null) {
            processConfirmedEngine(autoSelectedEngine.engineId)
        }
        updateUiControlFlags()
    }

    /** Called when the user selects an engine from the dropdown (before confirming). */
    fun selectTemporaryEngine(engine: EngineInfoDto) {
        _uiState.update { it.copy(temporarilySelectedEngine = engine) }
        // No further processing, wait for confirmation
    }

    /** Called when the user clicks "Confirm Engine". */
    fun confirmEngineSelection() {
        val engineToConfirm = _uiState.value.temporarilySelectedEngine ?: return
        resetSelectionsBelow(SelectionLevel.ENGINE) // Reset body/model when engine is confirmed
        _uiState.update {
            it.copy(
                confirmedEngine = engineToConfirm,
                isEditingEngine = false, // Exit editing mode
                temporarilySelectedEngine = null // Clear temporary selection
            )
        }
        // Process the newly confirmed engine to find available bodies
        processConfirmedEngine(engineToConfirm.engineId)
    }

    /** Called when the user clicks "Edit Engine". */
    fun editEngineSelection() {
        // Reset body/model as engine choice might change
        resetSelectionsBelow(SelectionLevel.ENGINE)
        _uiState.update {
            it.copy(
                isEditingEngine = true,
                // Pre-fill temporary selection with the currently confirmed one
                temporarilySelectedEngine = it.confirmedEngine
            )
        }
        updateUiControlFlags()
    }

    /** Filters bodies based on the confirmed engine and auto-confirms if unique. */
    private fun processConfirmedEngine(confirmedEngineId: Int) {
        val selectedSeriesData = _uiState.value.selectedSeriesDto ?: return
        val selectedYear = _uiState.value.selectedYear ?: return

        // Find models matching year and containing the confirmed engine
        val modelsWithEngine = selectedSeriesData.vehicleModelInfo
            .filter { it.year == selectedYear && it.engineInfo.any { engine -> engine.engineId == confirmedEngineId } }

        val availableBodies = modelsWithEngine
            .flatMap { it.bodyInfo }
            .distinctBy { it.bodyId }

        val autoSelectedBody = if (availableBodies.size == 1) availableBodies.first() else null

        _uiState.update {
            it.copy(
                availableBodies = availableBodies,
                confirmedBody = autoSelectedBody, // Confirm directly if unique
                isEditingBody = false, // Ensure not editing if auto-confirmed
                temporarilySelectedBody = null, // Clear temporary selection
                // Reset downstream state implicitly
            )
        }

        // If body was auto-confirmed, proceed to determine final model(s)
        if (autoSelectedBody != null) {
            processConfirmedBody(autoSelectedBody.bodyId)
        }
        updateUiControlFlags()
    }

    /** Called when the user selects a body from the dropdown (before confirming). */
    fun selectTemporaryBody(body: BodyInfoDto) {
        _uiState.update { it.copy(temporarilySelectedBody = body) }
        // No further processing, wait for confirmation
    }

    /** Called when the user clicks "Confirm Body". */
    fun confirmBodySelection() {
        val bodyToConfirm = _uiState.value.temporarilySelectedBody ?: return
        resetSelectionsBelow(SelectionLevel.BODY) // Reset model when body is confirmed
        _uiState.update {
            it.copy(
                confirmedBody = bodyToConfirm,
                isEditingBody = false, // Exit editing mode
                temporarilySelectedBody = null // Clear temporary selection
            )
        }
        // Process the newly confirmed body to find the final model(s)
        processConfirmedBody(bodyToConfirm.bodyId)
    }

    /** Called when the user clicks "Edit Body". */
    fun editBodySelection() {
        resetSelectionsBelow(SelectionLevel.BODY) // Reset model when editing body
        _uiState.update {
            it.copy(
                isEditingBody = true,
                // Pre-fill temporary selection with the currently confirmed one
                temporarilySelectedBody = it.confirmedBody
            )
        }
        updateUiControlFlags()
    }

    /** Filters final models based on confirmed engine/body and auto-selects if unique. */
    private fun processConfirmedBody(confirmedBodyId: Int) {
        val selectedSeriesData = _uiState.value.selectedSeriesDto ?: return
        val selectedYear = _uiState.value.selectedYear ?: return
        val confirmedEngineId = _uiState.value.confirmedEngine?.engineId ?: return

        // Find models matching Year, confirmed Engine ID, AND confirmed Body ID
        val finalModels = selectedSeriesData.vehicleModelInfo
            .filter { model ->
                model.year == selectedYear &&
                        model.engineInfo.any { engine -> engine.engineId == confirmedEngineId } &&
                        model.bodyInfo.any { body -> body.bodyId == confirmedBodyId }
            }

        val needsModel = finalModels.size > 1
        // Auto-select model only if unique *after* engine/body confirmed
        val finalModelId = if (!needsModel && finalModels.isNotEmpty()) finalModels.first().modelId else null

        _uiState.update {
            it.copy(
                availableModels = finalModels,
                selectedModelId = finalModelId // Auto-select if unique
                // Don't set isSelectionComplete here, do it in updateUiControlFlags
            )
        }
        updateUiControlFlags() // Check completion state after potential model auto-selection
    }

    /** Called only if `needsModelSelection` is true and user picks a model from dropdown. */
    fun selectModel(model: ModelDecodedDto) {
        _uiState.update { it.copy(selectedModelId = model.modelId) }
        updateUiControlFlags() // Check if selection is now complete
    }

    // --- Helper Methods ---

    /** Updates all UI control flags based on the current state. */
    private fun updateUiControlFlags() {
        _uiState.update { currentState ->
            val needsProducer = currentState.availableProducers.size > 1 && currentState.selectedProducer == null
            val needsSeries = currentState.selectedProducer != null && currentState.availableSeries.size > 1 && currentState.selectedSeriesDto == null
            val needsYear = currentState.selectedSeriesDto != null && currentState.availableYears.size > 1 && currentState.selectedYear == null

            // Engine confirmation needed only if options > 1 and not yet confirmed
            val needsEngineConfirm = currentState.selectedYear != null && currentState.availableEngines.size > 1 && currentState.confirmedEngine == null
            // Body confirmation needed only if engine confirmed, options > 1, and body not yet confirmed
            val needsBodyConfirm = currentState.confirmedEngine != null && currentState.availableBodies.size > 1 && currentState.confirmedBody == null
            // Model selection needed only if body confirmed, final models > 1, and model not yet selected
            val needsModelSel = currentState.confirmedBody != null && currentState.availableModels.size > 1 && currentState.selectedModelId == null

            // Show engine dropdown if editing OR if confirmation is needed
            val showEngDd = currentState.isEditingEngine || needsEngineConfirm
            // Show body dropdown if editing OR if confirmation is needed
            val showBodyDd = currentState.isEditingBody || needsBodyConfirm

            // Check overall completion
            var complete = currentState.selectedProducer != null &&
                    currentState.selectedSeriesDto != null &&
                    currentState.selectedYear != null &&
                    // Engine/Body must be confirmed if options were available
                    (currentState.availableEngines.isEmpty() || currentState.confirmedEngine != null) &&
                    (currentState.availableBodies.isEmpty() || currentState.confirmedBody != null) &&
                    // Model must be selected if selection was needed
                    (!needsModelSel || currentState.selectedModelId != null)


            currentState.copy(
                needsProducerSelection = needsProducer,
                needsSeriesSelection = needsSeries,
                needsYearSelection = needsYear,
                needsEngineConfirmation = needsEngineConfirm,
                needsBodyConfirmation = needsBodyConfirm,
                needsModelSelection = needsModelSel,
                showEngineDropdown = showEngDd,
                showBodyDropdown = showBodyDd,
                isSelectionComplete = complete
            )
        }
    }

    /** Resets selections and state below a specified level. */
    private fun resetSelectionsBelow(level: SelectionLevel) {
        _uiState.update { currentState ->
            var updatedState = currentState // Start with current state

            if (level < SelectionLevel.SERIES) {
                updatedState = updatedState.copy(selectedSeriesDto = null, availableSeries = emptyList())
            }
            if (level < SelectionLevel.YEAR) {
                updatedState = updatedState.copy(selectedYear = null, availableYears = emptyList())
            }
            if (level < SelectionLevel.ENGINE) {
                updatedState = updatedState.copy(
                    availableEngines = emptyList(),
                    temporarilySelectedEngine = null, confirmedEngine = null, isEditingEngine = false
                )
            }
            if (level < SelectionLevel.BODY) {
                updatedState = updatedState.copy(
                    availableBodies = emptyList(),
                    temporarilySelectedBody = null, confirmedBody = null, isEditingBody = false
                )
            }
            if (level < SelectionLevel.MODEL) {
                updatedState = updatedState.copy(
                    availableModels = emptyList(),
                    selectedModelId = null
                )
            }
            // Always reset completion and save success flags when resetting
            updatedState = updatedState.copy(isSelectionComplete = false, isSaveSuccess = false)
            updatedState
        }
        // After resetting, update UI flags (this will also re-check completion)
        updateUiControlFlags()
    }

    // Defines the hierarchy for resetting selections
    private enum class SelectionLevel { PRODUCER, SERIES, YEAR, ENGINE, BODY, MODEL }

    // --- Action Handlers ---

    /** Called by UI after an error message has been shown. */
    fun errorShown() { _uiState.update { it.copy(error = null) } }

    /** Called by UI after successful save navigation has occurred. */
    fun resetSaveSuccess() { _uiState.update { it.copy(isSaveSuccess = false) } }

    /** Gathers confirmed details and triggers the save process. */
    fun confirmAndSaveVehicle() {
        if (!_uiState.value.isSelectionComplete) {
            _uiState.update { it.copy(error = "Please complete all selections.") }
            return
        }

        // Gather necessary IDs and data from the final confirmed state
        val finalModelId = _uiState.value.selectedModelId
        val finalEngineId = _uiState.value.confirmedEngine?.engineId
        val finalBodyId = _uiState.value.confirmedBody?.bodyId
        // You might need VIN, which could be part of the Series DTO or Model DTO depending on backend structure
        // val vin = _uiState.value.selectedSeriesDto?.vin // Example if VIN is at series level
        // val producer = _uiState.value.selectedProducer
        // val seriesName = _uiState.value.selectedSeriesDto?.seriesName
        // val year = _uiState.value.selectedYear

        // Validate that essential IDs are present (especially if engine/body could be empty lists initially)
        if (finalModelId == null || finalEngineId == null || finalBodyId == null) {
            Log.e("ConfirmVM", "Save aborted: Missing essential ID(s) - Model: $finalModelId, Engine: $finalEngineId, Body: $finalBodyId")
            _uiState.update { it.copy(error = "Internal error: Could not finalize vehicle details.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        Log.d("ConfirmVM", "--- SAVING VEHICLE ---")
        Log.d("ConfirmVM", "Producer: ${_uiState.value.selectedProducer}")
        Log.d("ConfirmVM", "Series: ${_uiState.value.selectedSeriesDto?.seriesName}")
        Log.d("ConfirmVM", "Year: ${_uiState.value.selectedYear}")
        Log.d("ConfirmVM", "Confirmed Engine ID: $finalEngineId")
        Log.d("ConfirmVM", "Confirmed Body ID: $finalBodyId")
        Log.d("ConfirmVM", "Final Model ID: $finalModelId")
        Log.d("ConfirmVM", "----------------------")

        // TODO: Implement actual save logic using injected repository
        viewModelScope.launch {
            // Example: Create a request DTO for saving
            // val saveRequest = VehicleSaveRequest(modelId = finalModelId, engineId = finalEngineId, ...)
            // val result = saveRepository.saveVehicle(saveRequest)
            // result.onSuccess {
            //     _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            // }.onFailure { exception ->
            //      Log.e("ConfirmVM", "Save failed", exception)
            //     _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Failed to save vehicle.") }
            // }

            // Placeholder for success/failure
            kotlinx.coroutines.delay(1500) // Simulate network call
            val success = true // Simulate success/failure
            if (success) {
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save vehicle (Simulated).") }
            }
        }
    }
}