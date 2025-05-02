package com.example.cartrack.feature.addvehicle.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository
// import com.example.cartrack.feature.addvehicle.domain.repository.SaveVehicleRepository // TODO: Inject this
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vinDecoderRepository: VinDecoderRepository,
    // TODO: Inject SaveVehicleRepository
    // private val saveVehicleRepository: SaveVehicleRepository,
    private val jwtDecoder: JwtDecoder // Make sure JwtDecoder is provided via Hilt
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    private val logTag = "AddVehicleVM"

    // --- Step 1: VIN Input ---

    fun onVinInputChange(newVin: String) {
        if (_uiState.value.currentStep != AddVehicleStep.VIN) return

        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase()
        if (processedVin.length <= 17) {
            val isValidLength = processedVin.length == 17
            _uiState.update {
                it.copy(
                    vinInput = processedVin,
                    vinValidationError = if (!isValidLength && processedVin.isNotEmpty()) "VIN must be 17 characters" else null,
                    error = null,
                    isNextEnabled = isValidLength
                )
            }
        }
    }

    fun decodeVinAndProceed() {
        val vin = _uiState.value.vinInput
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be exactly 17 characters.", isNextEnabled = false) }
            return
        }
        _uiState.update { it.copy(vinValidationError = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Cannot identify user. Please login again.", isPreviousEnabled = true) }
                return@launch
            }

            val result = vinDecoderRepository.decodeVin(vin, clientId)

            result.onSuccess { decodedInfo ->
                if (decodedInfo.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No vehicle information found for this VIN.",
                            isPreviousEnabled = true
                        )
                    }
                } else {
                    processVinDecodeResults(decodedInfo) // Advances step on success
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to decode VIN.",
                        isPreviousEnabled = true
                    )
                }
            }
        }
    }

    // --- Processing Decoded Results & Selection Cascade ---

    private fun processVinDecodeResults(results: List<VinDecodedResponseDto>) {
        Log.d(logTag, "Processing ${results.size} decoded options.")
        _uiState.update { initialState ->
            initialState.copy(
                isLoading = false,
                allDecodedOptions = results,
                selectedProducer = null, availableProducers = emptyList(), needsProducerSelection = false,
                selectedSeriesDto = null, availableSeries = emptyList(), needsSeriesSelection = false,
                determinedYear = null,
                confirmedEngine = null, availableEngines = emptyList(), needsEngineConfirmation = false,
                confirmedBody = null, availableBodies = emptyList(), needsBodyConfirmation = false,
                selectedModelId = null, availableModels = emptyList(), needsModelSelection = false,
                mileageInput = "", mileageValidationError = null,
                isSaveSuccess = false,
                error = null,
                currentStep = AddVehicleStep.SERIES // Move to SERIES step *after* processing
            )
        }
        analyzeProducers() // Analyze options for the new step
    }

    private fun analyzeProducers() {
        val results = _uiState.value.allDecodedOptions
        val uniqueProducers = results.mapNotNull { it.producer }.distinct().sorted()
        val needsProducer = uniqueProducers.size > 1
        val autoSelectedProducer = if (!needsProducer && uniqueProducers.isNotEmpty()) uniqueProducers.first() else null

        _uiState.update {
            it.copy(
                availableProducers = uniqueProducers,
                needsProducerSelection = needsProducer,
                selectedProducer = autoSelectedProducer,
            )
        }
        if (autoSelectedProducer != null) {
            filterSeriesForProducer(autoSelectedProducer)
        }
        updateButtonStates() // Update for SERIES step
    }

    fun selectProducer(producer: String) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Producer selected: $producer")
        resetSelectionsBelow(AddVehicleStep.SERIES)
        _uiState.update { it.copy(selectedProducer = producer) }
        filterSeriesForProducer(producer)
        updateButtonStates()
    }

    private fun filterSeriesForProducer(producer: String) {
        val seriesForProducer = _uiState.value.allDecodedOptions.filter { it.producer == producer }
        val needsSeries = seriesForProducer.size > 1
        val autoSelectedSeries = if (!needsSeries && seriesForProducer.isNotEmpty()) seriesForProducer.first() else null

        _uiState.update {
            it.copy(
                availableSeries = seriesForProducer,
                needsSeriesSelection = needsSeries,
                selectedSeriesDto = autoSelectedSeries,
            )
        }
        if (autoSelectedSeries != null) {
            determineYearAndFilterEngines(autoSelectedSeries)
        }
        updateButtonStates()
    }

    fun selectSeries(seriesDto: VinDecodedResponseDto) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Series selected: ${seriesDto.seriesName}")
        resetSelectionsBelow(AddVehicleStep.ENGINE)
        _uiState.update { it.copy(selectedSeriesDto = seriesDto) }
        determineYearAndFilterEngines(seriesDto)
        updateButtonStates()
    }

    private fun determineYearAndFilterEngines(seriesDto: VinDecodedResponseDto) {
        val allModels = seriesDto.vehicleModelInfo
        val uniqueYears = allModels.mapNotNull { it.year }.distinct()

        if (uniqueYears.size == 1) {
            val determinedYear = uniqueYears.first()
            Log.d(logTag, "Year automatically determined: $determinedYear")
            _uiState.update { it.copy(determinedYear = determinedYear) }
            filterEnginesForYear(determinedYear)
        } else {
            val errorMsg = "Error: Could not uniquely determine year for series '${seriesDto.seriesName}'. VIN data issue."
            Log.e(logTag, errorMsg)
            _uiState.update { it.copy(error = errorMsg, isNextEnabled = false) }
        }
        updateButtonStates() // Update for SERIES step
    }

    private fun filterEnginesForYear(year: Int) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val modelsForYear = seriesDto.vehicleModelInfo.filter { it.year == year }
        val availableEngines = modelsForYear.flatMap { it.engineInfo }.distinctBy { it.engineId }
        val needsEngine = availableEngines.size > 1
        val autoSelectedEngine = if (!needsEngine && availableEngines.isNotEmpty()) availableEngines.first() else null

        _uiState.update {
            it.copy(
                availableEngines = availableEngines,
                needsEngineConfirmation = needsEngine,
                confirmedEngine = autoSelectedEngine,
            )
        }
        if (autoSelectedEngine != null) {
            filterBodiesForEngine(autoSelectedEngine.engineId)
        } else if (availableEngines.isEmpty()) {
            Log.w(logTag, "No engines found for Year $year, preparing for Body step options.")
            filterBodiesForEngine(null)
        }
        updateButtonStates() // Update for ENGINE step
    }

    fun selectEngine(engine: EngineInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.ENGINE) return
        Log.d(logTag, "Engine selected: ${engine.displayString()}")
        resetSelectionsBelow(AddVehicleStep.BODY)
        _uiState.update { it.copy(confirmedEngine = engine) }
        filterBodiesForEngine(engine.engineId)
        updateButtonStates()
    }

    private fun filterBodiesForEngine(engineId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return

        val modelsForEngine = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year && (engineId == null || model.engineInfo.any { it.engineId == engineId })
        }
        val availableBodies = modelsForEngine.flatMap { it.bodyInfo }.distinctBy { it.bodyId }
        val needsBody = availableBodies.size > 1
        val autoSelectedBody = if (!needsBody && availableBodies.isNotEmpty()) availableBodies.first() else null

        _uiState.update {
            it.copy(
                availableBodies = availableBodies,
                needsBodyConfirmation = needsBody,
                confirmedBody = autoSelectedBody,
            )
        }
        if (autoSelectedBody != null) {
            checkFinalModelDisambiguation(autoSelectedBody.bodyId)
        } else if (availableBodies.isEmpty()) {
            Log.w(logTag, "No bodies found for Engine $engineId / Year $year, preparing for Mileage step options.")
            checkFinalModelDisambiguation(null)
        }
        updateButtonStates() // Update for BODY step
    }

    fun selectBody(body: BodyInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.BODY) return
        Log.d(logTag, "Body selected: ${body.displayString()}")
        resetSelectionsBelow(AddVehicleStep.MILEAGE)
        _uiState.update { it.copy(confirmedBody = body) }
        checkFinalModelDisambiguation(body.bodyId)
        updateButtonStates()
    }

    private fun checkFinalModelDisambiguation(bodyId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return
        val engineId = _uiState.value.confirmedEngine?.engineId

        val finalMatchingModels = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year &&
                    (engineId == null || model.engineInfo.any { it.engineId == engineId }) &&
                    (bodyId == null || model.bodyInfo.any { it.bodyId == bodyId })
        }
        val needsModel = finalMatchingModels.size > 1
        val autoSelectedModelId = if (!needsModel && finalMatchingModels.isNotEmpty()) finalMatchingModels.first().modelId else null

        Log.d(logTag, "Final model check: ${finalMatchingModels.size} matching models. Needs selection: $needsModel. Auto-selected ID: $autoSelectedModelId")

        _uiState.update {
            it.copy(
                availableModels = if (needsModel) finalMatchingModels else emptyList(),
                needsModelSelection = needsModel,
                selectedModelId = autoSelectedModelId,
            )
        }
        updateButtonStates() // Update for MILEAGE step
    }

    fun selectModel(model: ModelDecodedDto) {
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE || !_uiState.value.needsModelSelection) return
        Log.d(logTag, "Final Model selected: ID ${model.modelId}")
        _uiState.update { it.copy(selectedModelId = model.modelId) }
        updateButtonStates() // Update for MILEAGE step
    }

    // --- Step 5: Mileage Input ---
    fun onMileageChange(mileage: String) {
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE) return
        val digitsOnly = mileage.filter { it.isDigit() }.take(9)
        val isValid = digitsOnly.isNotBlank() && digitsOnly.toLongOrNull() != null && digitsOnly.toLong() >= 0

        _uiState.update {
            it.copy(
                mileageInput = digitsOnly,
                mileageValidationError = if (digitsOnly.isNotEmpty() && !isValid) "Invalid mileage" else null,
                // Update button state based on validity AND model selection
                isNextEnabled = isValid && (!it.needsModelSelection || it.selectedModelId != null)
            )
        }
        // We still need to call updateButtonStates to consider the isLoading flag etc.
        // Although modifying isNextEnabled here covers most cases for the MILEAGE step.
        updateButtonStates()
    }

    // --- Navigation and State Reset ---

    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoading) {
            Log.d(logTag, "Next ignored: Enabled=${currentState.isNextEnabled}, Loading=${currentState.isLoading}")
            return // Guard against invalid clicks
        }

        if (currentState.error != null) _uiState.update { it.copy(error = null) }

        // Determine next step based on current (BEFORE updating state)
        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal >= AddVehicleStep.values().size) {
            Log.w(logTag, "Already at the last step (CONFIRM), cannot go next.")
            return // Should be handled by Save button anyway
        }
        val nextStep = AddVehicleStep.values()[nextStepOrdinal]


        // Special handling for VIN step -> Trigger decode first
        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndProceed() // Decode handles step change on success via processVinDecodeResults
        } else {
            // For all other steps, just advance the step
            Log.d(logTag, "Moving from ${currentState.currentStep} to $nextStep")
            _uiState.update { it.copy(currentStep = nextStep) }
            updateButtonStates() // Update buttons for the *new* step
        }
    }

    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoading) return

        val previousStep = AddVehicleStep.values().last { it.ordinal < currentStep.ordinal }
        Log.d(logTag, "Moving back from $currentStep to $previousStep")

        resetSelectionsBelow(previousStep)

        _uiState.update {
            it.copy(
                currentStep = previousStep,
                error = null,
                mileageValidationError = null,
                vinValidationError = null
            )
        }
        updateButtonStates()
    }

    private fun resetSelectionsBelow(targetStep: AddVehicleStep) {
        _uiState.update { currentState ->
            var updated = currentState
            if (targetStep < AddVehicleStep.SERIES) {
                updated = updated.copy(selectedProducer = null, availableProducers = emptyList(), needsProducerSelection = false, selectedSeriesDto = null, availableSeries = emptyList(), needsSeriesSelection = false, determinedYear = null)
            }
            if (targetStep < AddVehicleStep.ENGINE) {
                updated = updated.copy(confirmedEngine = null, availableEngines = emptyList(), needsEngineConfirmation = false)
            }
            if (targetStep < AddVehicleStep.BODY) {
                updated = updated.copy(confirmedBody = null, availableBodies = emptyList(), needsBodyConfirmation = false)
            }
            if (targetStep < AddVehicleStep.MILEAGE) {
                updated = updated.copy(mileageInput = "", mileageValidationError = null, selectedModelId = null, availableModels = emptyList(), needsModelSelection = false)
            }
            updated = updated.copy(isSaveSuccess = false)
            updated
        }
        Log.d(logTag, "Reset state below step: $targetStep")
    }

    private fun updateButtonStates() {
        _uiState.update { state ->
            val isPrevEnabled = state.currentStep != AddVehicleStep.VIN && !state.isLoading
            var isNextEnabled = !state.isLoading && state.error == null // Start with basic enabled state (not loading, no blocking error)

            // Check conditions specific to the *current* step to potentially disable Next
            when (state.currentStep) {
                AddVehicleStep.VIN -> isNextEnabled = isNextEnabled && state.vinInput.length == 17 && state.vinValidationError == null
                AddVehicleStep.SERIES -> isNextEnabled = isNextEnabled && state.selectedProducer != null && (!state.needsSeriesSelection || state.selectedSeriesDto != null) && state.determinedYear != null // Year check added
                AddVehicleStep.ENGINE -> isNextEnabled = isNextEnabled && (!state.needsEngineConfirmation || state.confirmedEngine != null)
                AddVehicleStep.BODY -> isNextEnabled = isNextEnabled && (!state.needsBodyConfirmation || state.confirmedBody != null)
                AddVehicleStep.MILEAGE -> isNextEnabled = isNextEnabled && state.mileageInput.isNotBlank() && state.mileageValidationError == null && state.mileageInput.toLongOrNull() != null && (!state.needsModelSelection || state.selectedModelId != null)
                AddVehicleStep.CONFIRM -> isNextEnabled = false // 'Next' button effectively disabled (becomes Save)
            }

            state.copy(
                isPreviousEnabled = isPrevEnabled,
                isNextEnabled = isNextEnabled
                // Note: Save button enabled state is handled directly in the UI based on currentStep and isLoading
            )
        }
    }

    // --- Step 6: Confirmation & Save ---
    fun saveVehicle() {
        val state = _uiState.value
        val year = state.determinedYear
        val mileage = state.mileageInput.toLongOrNull()
        val finalModelId = state.selectedModelId
        val confirmedEngineId = state.confirmedEngine?.engineId
        val confirmedBodyId = state.confirmedBody?.bodyId

        // --- Validation ---
        var validationError: String? = null
        if (year == null) validationError = "Could not determine vehicle year."
        else if (mileage == null || mileage < 0) validationError = "Invalid mileage entered."
        else if (state.needsEngineConfirmation && confirmedEngineId == null) validationError = "Engine details missing."
        else if (state.needsBodyConfirmation && confirmedBodyId == null) validationError = "Body style missing."
        else if (state.needsModelSelection && finalModelId == null) validationError = "Specific model selection missing."
        else if (state.selectedProducer == null || state.selectedSeriesDto == null) validationError = "Make/Series information missing."

        if (validationError != null) {
            _uiState.update { it.copy(error = "Cannot save: $validationError") }
            Log.e(logTag, "Save validation failed: $validationError")
            return
        }

        // --- Proceed with Save ---
        _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }
        Log.d(logTag, "--- Attempting to Save Vehicle ---")
        // ... (logging details) ...

        viewModelScope.launch {
            // TODO: Implement actual save logic using SaveVehicleRepository
            kotlinx.coroutines.delay(1500) // Simulate network
            val success = true // Simulate result

            if (success) {
                Log.i(logTag, "Vehicle saved successfully.")
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            } else {
                val saveErrorMsg = "Failed to save vehicle (Simulated)."
                Log.e(logTag, "Save failed: $saveErrorMsg")
                _uiState.update { it.copy(isLoading = false, error = saveErrorMsg, isPreviousEnabled = true) }
            }
        }
    }

    fun errorShown() {
        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(error = null) }
        }
    }

    fun saveSuccessNavigationComplete() {
        _uiState.update { it.copy(isSaveSuccess = false) }
        // Optionally reset to initial state here if desired after saving
        // _uiState.value = AddVehicleUiState()
    }
}