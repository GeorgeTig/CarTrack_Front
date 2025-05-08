package com.example.cartrack.feature.addvehicle.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.addvehicle.domain.repository.SaveVehicleRepository
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository
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
    private val saveVehicleRepository: SaveVehicleRepository, // Assumed interface for saving
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    private val logTag = "AddVehicleVM"

    /** Updates the VIN input string and performs basic validation. */
    fun onVinInputChange(newVin: String) {
        if (_uiState.value.currentStep != AddVehicleStep.VIN) return

        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase().take(17)
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

    /** Stores the selected producer and filters the available series. */
    fun selectProducer(producer: String) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Producer selected: $producer")
        resetOptionsStrictlyBelow(AddVehicleStep.SERIES)
        _uiState.update { it.copy(selectedProducer = producer) }
        filterSeriesForProducer(producer)
        updateButtonStates()
    }

    /** Stores the selected series DTO, determines the year, and triggers engine filtering prep. */
    fun selectSeries(seriesDto: VinDecodedResponseDto) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Series selected: ${seriesDto.seriesName}")
        resetOptionsStrictlyBelow(AddVehicleStep.ENGINE)
        _uiState.update { it.copy(selectedSeriesDto = seriesDto) }
        determineYearAndFilterEngines(seriesDto)
        updateButtonStates()
    }

    /** Stores the confirmed engine and filters the available bodies. */
    fun selectEngine(engine: EngineInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.ENGINE) return
        Log.d(logTag, "Engine selected: ${engine.displayString()}")
        resetOptionsStrictlyBelow(AddVehicleStep.BODY)
        _uiState.update { it.copy(confirmedEngine = engine) }
        filterBodiesForEngine(engine.engineId)
        updateButtonStates()
    }

    /** Stores the confirmed body and checks if final model selection is needed. */
    fun selectBody(body: BodyInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.BODY) return
        Log.d(logTag, "Body selected: ${body.displayString()}")
        resetOptionsStrictlyBelow(AddVehicleStep.MILEAGE)
        _uiState.update { it.copy(confirmedBody = body) }
        checkFinalModelDisambiguation(body.bodyId)
        updateButtonStates()
    }

    /** Stores the final model ID if disambiguation was required. */
    fun selectModel(model: ModelDecodedDto) {
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE || !_uiState.value.needsModelSelection) return
        Log.d(logTag, "Final Model selected: ID ${model.modelId}")
        _uiState.update { it.copy(selectedModelId = model.modelId) }
        updateButtonStates()
    }

    /** Updates the mileage input string and performs validation. */
    fun onMileageChange(mileage: String) {
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE) return
        val digitsOnly = mileage.filter { it.isDigit() }.take(9)
        val isValid = digitsOnly.isNotBlank() && digitsOnly.toLongOrNull()?.let { it >= 0 } ?: false
        _uiState.update {
            it.copy(
                mileageInput = digitsOnly,
                mileageValidationError = if (digitsOnly.isNotEmpty() && !isValid) "Invalid mileage" else null
            )
        }
        updateButtonStates()
    }

    /** Initiates the VIN decoding API call. Called when Next is clicked on VIN step. */
    internal fun decodeVinAndProceed() {
        val vin = _uiState.value.vinInput
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be 17 characters.", isNextEnabled = false) }
            return
        }
        _uiState.update { it.copy(vinValidationError = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Cannot identify user.", isPreviousEnabled = true) }
                return@launch
            }

            Log.d(logTag, "Decoding VIN: $vin")
            val result = vinDecoderRepository.decodeVin(vin, clientId)
            result.onSuccess { decodedInfo ->
                Log.d(logTag, "VIN Decode Success: ${decodedInfo.size} options found.")
                if (decodedInfo.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No vehicle info found for VIN.", isPreviousEnabled = true) }
                } else {
                    processVinDecodeResults(decodedInfo) // Advances step and updates buttons
                }
            }.onFailure { exception ->
                Log.e(logTag, "VIN Decode Failed", exception)
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Failed to decode VIN.", isPreviousEnabled = true) }
            }
        }
    }

    /** Processes the API response, resets state, and prepares the SERIES step. */
    private fun processVinDecodeResults(results: List<VinDecodedResponseDto>) {
        Log.d(logTag, "Processing VIN results.")
        _uiState.update {
            AddVehicleUiState( // Create fresh state, keeping only VIN input
                vinInput = it.vinInput,
                currentStep = AddVehicleStep.SERIES,
                allDecodedOptions = results
            )
        }
        prepareDataForStep(AddVehicleStep.SERIES)
        updateButtonStates()
    }

    /** Determines available producers and auto-selects if only one exists. */
    private fun analyzeProducers() {
        val results = _uiState.value.allDecodedOptions
        val uniqueProducers = results.map { it.producer }.distinct().sorted()
        val needsProducer = uniqueProducers.size > 1

        Log.d(logTag, "Producers Available: ${uniqueProducers.size}")
        _uiState.update {
            it.copy(
                availableProducers = uniqueProducers,
                needsProducerSelection = true
            )
        }
        updateButtonStates()
    }


    /** Filters available series based on the selected producer. */
    private fun filterSeriesForProducer(producer: String) {
        val seriesForProducer = _uiState.value.allDecodedOptions.filter { it.producer == producer }
        val needsSeries = seriesForProducer.size > 1

        Log.d(logTag, "Series for '$producer': ${seriesForProducer.size} options.")
        _uiState.update {
            it.copy(
                availableSeries = seriesForProducer,
                needsSeriesSelection = true
            )
        }
        updateButtonStates()
    }

    /** Determines the unique year from the selected series DTO. */
    private fun determineYearAndFilterEngines(seriesDto: VinDecodedResponseDto) {
        val uniqueYears = seriesDto.vehicleModelInfo.map { it.year }.distinct()
        val determinedYearValue = if (uniqueYears.size == 1) uniqueYears.first() else null
        val errorMsg = if (determinedYearValue == null && uniqueYears.isNotEmpty()) "Error: Could not determine year for '${seriesDto.seriesName}'." else null

        Log.d(logTag, "Year determined for '${seriesDto.seriesName}': $determinedYearValue")
        _uiState.update { it.copy(determinedYear = determinedYearValue, error = errorMsg) }
    }


    /** Filters available engines based on the determined year. */
    private fun filterEnginesForYear(year: Int) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        Log.d(logTag, "Filtering engines for year $year")

        val modelsForYear = seriesDto.vehicleModelInfo.filter { it.year == year }
        val availableEngines = modelsForYear.flatMap { it.engineInfo }.distinctBy { it.engineId }
        val needsEngine = availableEngines.size > 1

        Log.d(logTag, "Engines Available: ${availableEngines.size}")
        _uiState.update {
            it.copy(
                availableEngines = availableEngines,
                needsEngineConfirmation = true,
                confirmedEngine = if (it.confirmedEngine != null && availableEngines.any { eng -> eng.engineId == it.confirmedEngine?.engineId }) it.confirmedEngine else null // Keep existing valid selection, otherwise null
            )
        }
        updateButtonStates()
    }

    /** Filters available bodies based on determined year and confirmed engine. */
    private fun filterBodiesForEngine(engineId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return
        Log.d(logTag, "Filtering bodies for engineId: $engineId / year: $year")

        val modelsForEngine = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year && (engineId == null || model.engineInfo.any { it.engineId == engineId })
        }
        val availableBodies = modelsForEngine.flatMap { it.bodyInfo }.distinctBy { it.bodyId }
        val needsBody = availableBodies.size > 1

        Log.d(logTag, "Bodies Available: ${availableBodies.size}")
        _uiState.update {
            it.copy(
                availableBodies = availableBodies,
                needsBodyConfirmation = true,
                confirmedBody = if (it.confirmedBody != null && availableBodies.any { bod -> bod.bodyId == it.confirmedBody?.bodyId }) it.confirmedBody else null // Keep existing valid selection, otherwise null
            )
        }
         updateButtonStates()
    }

    /** Checks if final model selection is needed based on remaining unique model IDs. */
    private fun checkFinalModelDisambiguation(bodyId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return
        val engineId = _uiState.value.confirmedEngine?.engineId
        Log.d(logTag, "Checking final model for body: $bodyId / engine: $engineId / year: $year")

        val finalMatchingModels = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year &&
                    (engineId == null || model.engineInfo.any { it.engineId == engineId }) &&
                    (bodyId == null || model.bodyInfo.any { it.bodyId == bodyId })
        }
        val needsModel = finalMatchingModels.size > 1
        // AUTO-SELECT MODEL ID *ONLY* IF needsModel is FALSE
        val autoSelectedModelId = if (!needsModel) finalMatchingModels.firstOrNull()?.modelId else null

        Log.d(logTag, "Final Model Check: ${finalMatchingModels.size} matches. NeedsSelection=$needsModel, AutoSelectedID=$autoSelectedModelId")
        _uiState.update {
            it.copy(
                availableModels = if (needsModel) finalMatchingModels else emptyList(), // Only show options if needed
                needsModelSelection = needsModel,
                selectedModelId = if (needsModel) {
                    if (it.selectedModelId != null && finalMatchingModels.any { m -> m.modelId == it.selectedModelId }) it.selectedModelId else null
                } else {
                    autoSelectedModelId
                }
            )
        }
        updateButtonStates()
    }

    /** Handles moving to the next step in the flow. */
    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoading) return
        if (currentState.error != null) _uiState.update { it.copy(error = null) }

        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal >= AddVehicleStep.values().size) return

        val nextStep = AddVehicleStep.values()[nextStepOrdinal]

        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndProceed() // Decodes and potentially advances step
        } else {
            Log.d(logTag, "Moving from ${currentState.currentStep} to $nextStep")
            _uiState.update { it.copy(currentStep = nextStep) }
            prepareDataForStep(nextStep)
            updateButtonStates()
        }
    }

    /** Handles moving to the previous step in the flow. */
    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoading) return

        val previousStep = AddVehicleStep.values().last { it.ordinal < currentStep.ordinal }
        Log.d(logTag, "Moving back from $currentStep to $previousStep")

        resetOptionsStrictlyBelow(previousStep) // Resets available options, keeps selections

        _uiState.update {
            it.copy(
                currentStep = previousStep,
                error = null,
                mileageValidationError = null,
                vinValidationError = null
            )
        }
        prepareDataForStep(previousStep)
        updateButtonStates()
    }

    /** Prepares necessary data (e.g., filters options) for the specified step. */
    private fun prepareDataForStep(step: AddVehicleStep) {
        Log.d(logTag, "Preparing data for step: $step")
        val currentState = _uiState.value
        when (step) {
            AddVehicleStep.SERIES -> { if (currentState.selectedProducer == null) analyzeProducers() } // Analyze only if no producer selected yet
            AddVehicleStep.ENGINE -> { currentState.determinedYear?.let { filterEnginesForYear(it) } ?: Log.e(logTag, "Cannot prepare ENGINE: year missing.") }
            AddVehicleStep.BODY -> { filterBodiesForEngine(currentState.confirmedEngine?.engineId) } // Filter based on potentially selected engine
            AddVehicleStep.MILEAGE -> { checkFinalModelDisambiguation(currentState.confirmedBody?.bodyId) } // Check if final model selection is needed
            AddVehicleStep.VIN, AddVehicleStep.CONFIRM -> {  }
        }
    }

    /** Resets available selection lists and requirement flags for steps after the target step. */
    private fun resetOptionsStrictlyBelow(targetStep: AddVehicleStep) {
        _uiState.update { currentState ->
            var updated = currentState
            if (targetStep < AddVehicleStep.ENGINE) updated = updated.copy(availableEngines = emptyList(), needsEngineConfirmation = false)
            if (targetStep < AddVehicleStep.BODY) updated = updated.copy(availableBodies = emptyList(), needsBodyConfirmation = false)
            if (targetStep < AddVehicleStep.MILEAGE) updated = updated.copy(mileageInput = "", mileageValidationError = null, availableModels = emptyList(), needsModelSelection = false)
            updated.copy(isSaveSuccess = false) // Always clear save success on going back
        }
        Log.d(logTag, "Reset options STRICTLY below step: $targetStep")
    }

    /** Updates the enabled state of the Next/Previous buttons based on current state validity. */
    private fun updateButtonStates() {
        _uiState.update { state ->
            val isPrevEnabled = state.currentStep != AddVehicleStep.VIN && !state.isLoading
            var isNextValid = !state.isLoading && state.error == null

            when (state.currentStep) {
                AddVehicleStep.VIN -> isNextValid = isNextValid && state.vinInput.length == 17 && state.vinValidationError == null
                AddVehicleStep.SERIES -> isNextValid = isNextValid && state.selectedProducer != null && state.selectedSeriesDto != null && state.determinedYear != null
                AddVehicleStep.ENGINE -> isNextValid = isNextValid && state.confirmedEngine != null
                AddVehicleStep.BODY -> isNextValid = isNextValid && state.confirmedBody != null
                AddVehicleStep.MILEAGE -> {
                    val mileageValid = state.mileageInput.isNotBlank() && state.mileageValidationError == null && state.mileageInput.toLongOrNull()?.let { it >= 0 } ?: false
                    val modelValid = !state.needsModelSelection || state.selectedModelId != null // Model must be selected *if* needed
                    isNextValid = isNextValid && mileageValid && modelValid
                }
                AddVehicleStep.CONFIRM -> isNextValid = false
            }
            state.copy(isPreviousEnabled = isPrevEnabled, isNextEnabled = isNextValid)
        }
        Log.d(logTag, "Button States Updated: Prev=${_uiState.value.isPreviousEnabled}, Next=${_uiState.value.isNextEnabled} for Step=${_uiState.value.currentStep}")
    }

    /** Validates final selections and triggers the save vehicle API call. */
    fun saveVehicle() {
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isLoading) return

        val state = _uiState.value
        val vin = state.vinInput
        val mileageDouble = state.mileageInput.toDoubleOrNull()

        // Determine final model ID (non-suspend)
        val finalModelId = if (state.needsModelSelection) {
            state.selectedModelId
        } else {
            state.selectedSeriesDto?.vehicleModelInfo?.find { model ->
                model.year == state.determinedYear &&
                        (state.confirmedEngine == null || model.engineInfo.any { it.engineId == state.confirmedEngine.engineId }) &&
                        (state.confirmedBody == null || model.bodyInfo.any { it.bodyId == state.confirmedBody.bodyId })
            }?.modelId
        }

        // Preliminary non-suspend validation
        val preliminaryValidationError = when {
            finalModelId == null -> "Could not determine vehicle model ID."
            vin.length != 17 -> "Invalid VIN."
            mileageDouble == null || mileageDouble < 0.0 -> "Invalid mileage."
            else -> null
        }
        if (preliminaryValidationError != null) {
            _uiState.update { it.copy(error = "Cannot save: $preliminaryValidationError") }
            Log.e(logTag, "Save validation failed (preliminary): $preliminaryValidationError")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

            val clientId = jwtDecoder.getClientIdFromToken()

            val validationError = when {
                clientId == null -> "User identification failed."
                finalModelId == null -> "Could not determine vehicle model ID."
                vin.length != 17 -> "Invalid VIN."
                mileageDouble == null || mileageDouble < 0.0 -> "Invalid mileage."
                state.needsProducerSelection && state.selectedProducer == null -> "Make/Producer required."
                state.needsSeriesSelection && state.selectedSeriesDto == null -> "Series required."
                state.determinedYear == null -> "Could not determine year."
                state.needsEngineConfirmation && state.confirmedEngine == null -> "Engine selection required."
                state.needsBodyConfirmation && state.confirmedBody == null -> "Body style selection required."
                else -> null
            }

            if (validationError != null) {
                _uiState.update { it.copy(isLoading = false, error = "Cannot save: $validationError", isPreviousEnabled = true) }
                Log.e(logTag, "Save validation failed (final): $validationError")
                return@launch
            }

            // Construct request DTO (use non-null assertions after validation)
            val saveRequest = VehicleSaveRequestDto(
                clientId = clientId!!,
                modelId = finalModelId!!,
                vin = vin,
                mileage = mileageDouble!!
            )

            Log.d(logTag, "--- Attempting to Save Vehicle ---")
            Log.d(logTag, " Request: $saveRequest")

            // Perform Save API Call
            val result = saveVehicleRepository.saveVehicle(saveRequest)

            result.onSuccess {
                Log.i(logTag, "Vehicle saved successfully.")
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
            }.onFailure { exception ->
                val saveErrorMsg = exception.message ?: "Failed to save vehicle."
                Log.e(logTag, "Save failed", exception)
                _uiState.update { it.copy(isLoading = false, error = saveErrorMsg, isPreviousEnabled = true) }
            }
        }
    }
}