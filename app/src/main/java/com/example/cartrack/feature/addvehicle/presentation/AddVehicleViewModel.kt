package com.example.cartrack.feature.addvehicle.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder // Ensure this is available and injected
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository
// TODO: Uncomment and implement these when Save feature is ready
// import com.example.cartrack.feature.addvehicle.domain.repository.SaveVehicleRepository
// import com.example.cartrack.feature.addvehicle.data.model.VehicleSaveRequest
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
    private val jwtDecoder: JwtDecoder // Ensure provided via Hilt
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    private val logTag = "AddVehicleVM"

    // --- Input/Selection Actions (Called from UI) ---

    fun onVinInputChange(newVin: String) {
        if (_uiState.value.currentStep != AddVehicleStep.VIN) return // Only allow changes in VIN step

        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase()
        if (processedVin.length <= 17) {
            val isValidLength = processedVin.length == 17
            _uiState.update {
                it.copy(
                    vinInput = processedVin,
                    // Show validation error immediately if length is wrong but not empty
                    vinValidationError = if (!isValidLength && processedVin.isNotEmpty()) "VIN must be 17 characters" else null,
                    error = null, // Clear general error on input change
                    // Update button state based on validity
                    isNextEnabled = isValidLength
                )
            }
        }
    }

    fun selectProducer(producer: String) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Producer selected: $producer")
        // Reset state for steps after Series
        resetOptionsStrictlyBelow(AddVehicleStep.SERIES) // Keep Series/Producer selection
        _uiState.update { it.copy(selectedProducer = producer) }
        // Filter available series based on the new producer
        filterSeriesForProducer(producer)
        // Update button states for the current step (SERIES)
        updateButtonStates()
    }

    fun selectSeries(seriesDto: VinDecodedResponseDto) {
        if (_uiState.value.currentStep != AddVehicleStep.SERIES) return
        Log.d(logTag, "Series selected: ${seriesDto.seriesName}")
        // Reset state for steps after Series (Engine onwards)
        resetOptionsStrictlyBelow(AddVehicleStep.ENGINE) // Keep Series/Producer/DeterminedYear selection
        _uiState.update { it.copy(selectedSeriesDto = seriesDto) }
        // Determine the year based on the selected series
        determineYearAndFilterEngines(seriesDto) // This will store determinedYear
        // Update button states for the current step (SERIES)
        updateButtonStates()
    }

    fun selectEngine(engine: EngineInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.ENGINE) return
        Log.d(logTag, "Engine selected: ${engine.displayString()}")
        // Reset state for steps after Engine (Body onwards)
        resetOptionsStrictlyBelow(AddVehicleStep.BODY) // Keep Engine selection
        _uiState.update { it.copy(confirmedEngine = engine) }
        // Filter available bodies based on the selected engine
        filterBodiesForEngine(engine.engineId)
        // Update button states for the current step (ENGINE)
        updateButtonStates()
    }

    fun selectBody(body: BodyInfoDto) {
        if (_uiState.value.currentStep != AddVehicleStep.BODY) return
        Log.d(logTag, "Body selected: ${body.displayString()}")
        // Reset state for steps after Body (Mileage onwards)
        resetOptionsStrictlyBelow(AddVehicleStep.MILEAGE) // Keep Body selection
        _uiState.update { it.copy(confirmedBody = body) }
        // Check if final model selection is needed
        checkFinalModelDisambiguation(body.bodyId)
        // Update button states for the current step (BODY)
        updateButtonStates()
    }

    fun selectModel(model: ModelDecodedDto) {
        // Allow selection only in MILEAGE step if needed
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE || !_uiState.value.needsModelSelection) return
        Log.d(logTag, "Final Model selected: ID ${model.modelId}")
        _uiState.update { it.copy(selectedModelId = model.modelId) }
        // Update button states for the current step (MILEAGE)
        updateButtonStates()
    }

    fun onMileageChange(mileage: String) {
        if (_uiState.value.currentStep != AddVehicleStep.MILEAGE) return
        val digitsOnly = mileage.filter { it.isDigit() }.take(9) // Limit length
        // Validate mileage input (must be non-blank and a valid non-negative number)
        val isValid = digitsOnly.isNotBlank() && digitsOnly.toLongOrNull() != null && digitsOnly.toLong() >= 0

        _uiState.update {
            it.copy(
                mileageInput = digitsOnly,
                mileageValidationError = if (digitsOnly.isNotEmpty() && !isValid) "Invalid mileage" else null
                // Button state is handled by updateButtonStates called below
            )
        }
        // Update button states considering mileage validity and final model selection status
        updateButtonStates()
    }

    // --- Core Logic: Decode VIN and Prepare Data for Steps ---

    // decodeVinAndProceed needs to be internal or public for the UI step composable
    internal fun decodeVinAndProceed() {
        val vin = _uiState.value.vinInput
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be exactly 17 characters.", isNextEnabled = false) }
            return
        }
        _uiState.update { it.copy(vinValidationError = null) } // Clear validation error if okay now

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Cannot identify user. Please login again.", isPreviousEnabled = true) }
                return@launch
            }

            Log.d(logTag, "Decoding VIN: $vin")
            val result = vinDecoderRepository.decodeVin(vin, clientId)

            result.onSuccess { decodedInfo ->
                Log.d(logTag, "VIN Decode Success: ${decodedInfo.size} options found.")
                if (decodedInfo.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No vehicle information found for this VIN.", isPreviousEnabled = true)
                    }
                } else {
                    // Process results, advances step, prepares data, updates buttons
                    processVinDecodeResults(decodedInfo)
                }
            }.onFailure { exception ->
                Log.e(logTag, "VIN Decode Failed", exception)
                _uiState.update {
                    it.copy(isLoading = false, error = exception.message ?: "Failed to decode VIN.", isPreviousEnabled = true)
                }
            }
        }
    }

    // Processes the results from the VIN decode API call
    private fun processVinDecodeResults(results: List<VinDecodedResponseDto>) {
        Log.d(logTag, "Processing VIN results.")
        // Reset state completely, move to SERIES step
        _uiState.update { initialState ->
            initialState.copy(
                isLoading = false, // Done loading
                allDecodedOptions = results,
                // Reset all selections and available options
                selectedProducer = null, availableProducers = emptyList(), needsProducerSelection = false,
                selectedSeriesDto = null, availableSeries = emptyList(), needsSeriesSelection = false,
                determinedYear = null,
                confirmedEngine = null, availableEngines = emptyList(), needsEngineConfirmation = false,
                confirmedBody = null, availableBodies = emptyList(), needsBodyConfirmation = false,
                selectedModelId = null, availableModels = emptyList(), needsModelSelection = false,
                mileageInput = "", mileageValidationError = null,
                // Reset flags
                isSaveSuccess = false,
                error = null,
                // Advance step
                currentStep = AddVehicleStep.SERIES
            )
        }
        // Prepare data for the new step (SERIES)
        prepareDataForStep(AddVehicleStep.SERIES)
        // Update buttons for the new step
        updateButtonStates()
    }

    // Analyzes producers from the decoded options for the SERIES step
    private fun analyzeProducers() {
        val results = _uiState.value.allDecodedOptions
        val uniqueProducers = results.mapNotNull { it.producer }.distinct().sorted()
        val needsProducer = uniqueProducers.size > 1
        val autoSelectedProducer = if (!needsProducer && uniqueProducers.isNotEmpty()) uniqueProducers.first() else null

        Log.d(logTag, "Analyzing Producers: NeedsSelection=$needsProducer, AutoSelected=$autoSelectedProducer")
        _uiState.update {
            it.copy(
                availableProducers = uniqueProducers,
                needsProducerSelection = needsProducer,
                selectedProducer = autoSelectedProducer // Store selection (even if null)
            )
        }
        // If producer was auto-selected, filter the series for it immediately
        if (autoSelectedProducer != null) {
            filterSeriesForProducer(autoSelectedProducer)
        }
        // Note: updateButtonStates is called by the caller (processVinDecodeResults or prepareDataForStep)
    }

    // Filters available series based on the selected producer for the SERIES step
    private fun filterSeriesForProducer(producer: String) {
        val seriesForProducer = _uiState.value.allDecodedOptions.filter { it.producer == producer }
        val needsSeries = seriesForProducer.size > 1
        val autoSelectedSeries = if (!needsSeries && seriesForProducer.isNotEmpty()) seriesForProducer.first() else null

        Log.d(logTag, "Filtering Series for '$producer': NeedsSelection=$needsSeries, AutoSelected=${autoSelectedSeries?.seriesName}")
        _uiState.update {
            it.copy(
                availableSeries = seriesForProducer,
                needsSeriesSelection = needsSeries,
                selectedSeriesDto = autoSelectedSeries // Store selection (even if null)
            )
        }
        // If series was auto-selected, determine the year (but don't filter engines yet)
        if (autoSelectedSeries != null) {
            determineYearAndFilterEngines(autoSelectedSeries)
        }
        // Note: updateButtonStates is called by the caller (selectProducer or analyzeProducers)
    }

    // Determines the unique year from the selected series DTO for the SERIES step
    private fun determineYearAndFilterEngines(seriesDto: VinDecodedResponseDto) {
        val allModels = seriesDto.vehicleModelInfo
        val uniqueYears = allModels.mapNotNull { it.year }.distinct()

        if (uniqueYears.size == 1) {
            val determinedYear = uniqueYears.first()
            Log.d(logTag, "Year determined for '${seriesDto.seriesName}': $determinedYear")
            _uiState.update { it.copy(determinedYear = determinedYear, error = null) } // Store year, clear previous errors
            // Engine filtering is deferred until moving to ENGINE step via prepareDataForStep
        } else {
            // Error: Year is not unique as expected
            val errorMsg = "Error: Could not uniquely determine year for series '${seriesDto.seriesName}'. VIN data issue."
            Log.e(logTag, errorMsg)
            _uiState.update { it.copy(error = errorMsg, determinedYear = null, isNextEnabled = false) } // Clear year, disable next
        }
        // Note: updateButtonStates is called by the caller (selectSeries or filterSeriesForProducer)
    }

    // Filters available engines based on the determined year for the ENGINE step
    private fun filterEnginesForYear(year: Int) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        Log.d(logTag, "Filtering engines for year $year")

        val modelsForYear = seriesDto.vehicleModelInfo.filter { it.year == year }
        val availableEngines = modelsForYear.flatMap { it.engineInfo }.distinctBy { it.engineId }
        val needsEngine = availableEngines.size > 1
        val autoSelectedEngine = if (!needsEngine && availableEngines.isNotEmpty()) availableEngines.first() else null

        Log.d(logTag, "Available Engines: ${availableEngines.size}, NeedsSelection=$needsEngine, AutoSelected=${autoSelectedEngine?.engineId}")
        _uiState.update {
            it.copy(
                availableEngines = availableEngines,
                needsEngineConfirmation = needsEngine,
                // Check if current confirmed engine is still valid, otherwise use auto-selected or null
                confirmedEngine = if (it.confirmedEngine != null && availableEngines.any { eng -> eng.engineId == it.confirmedEngine?.engineId }) {
                    it.confirmedEngine // Keep existing valid selection
                } else {
                    autoSelectedEngine // Otherwise use auto-select (which could be null)
                }
            )
        }
        // If engine was auto-selected OR a valid one was kept, prepare body data
        val currentEngine = _uiState.value.confirmedEngine // Get potentially updated engine
        if (currentEngine != null) {
            filterBodiesForEngine(currentEngine.engineId)
        } else if (availableEngines.isEmpty()) {
            // If no engines, prepare body data without engine filter
            Log.w(logTag, "No engines found for Year $year, preparing body data.")
            filterBodiesForEngine(null)
        }
        // Note: updateButtonStates called by the caller (prepareDataForStep or selectEngine)
    }

    // Filters available bodies based on determined year and confirmed engine for the BODY step
    private fun filterBodiesForEngine(engineId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return
        Log.d(logTag, "Filtering bodies for engineId: $engineId / year: $year")

        val modelsForEngine = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year && (engineId == null || model.engineInfo.any { it.engineId == engineId })
        }
        val availableBodies = modelsForEngine.flatMap { it.bodyInfo }.distinctBy { it.bodyId }
        val needsBody = availableBodies.size > 1
        val autoSelectedBody = if (!needsBody && availableBodies.isNotEmpty()) availableBodies.first() else null

        Log.d(logTag, "Available Bodies: ${availableBodies.size}, NeedsSelection=$needsBody, AutoSelected=${autoSelectedBody?.bodyId}")
        _uiState.update {
            it.copy(
                availableBodies = availableBodies,
                needsBodyConfirmation = needsBody,
                // Check if current confirmed body is still valid, otherwise use auto-selected or null
                confirmedBody = if (it.confirmedBody != null && availableBodies.any { bod -> bod.bodyId == it.confirmedBody?.bodyId }) {
                    it.confirmedBody // Keep existing valid selection
                } else {
                    autoSelectedBody // Otherwise use auto-select (which could be null)
                }
            )
        }
        // If body was auto-selected OR a valid one was kept, check final model status
        val currentBody = _uiState.value.confirmedBody
        if (currentBody != null) {
            checkFinalModelDisambiguation(currentBody.bodyId)
        } else if (availableBodies.isEmpty()) {
            // If no bodies, check final model without body filter
            Log.w(logTag, "No bodies found for Engine $engineId / Year $year, checking final model.")
            checkFinalModelDisambiguation(null)
        }
        // Note: updateButtonStates called by the caller (prepareDataForStep or selectBody)
    }

    // Checks if final model selection is needed for the MILEAGE step
    private fun checkFinalModelDisambiguation(bodyId: Int?) {
        val seriesDto = _uiState.value.selectedSeriesDto ?: return
        val year = _uiState.value.determinedYear ?: return
        val engineId = _uiState.value.confirmedEngine?.engineId
        Log.d(logTag, "Checking final model disambiguation for bodyId: $bodyId / engineId: $engineId / year: $year")

        val finalMatchingModels = seriesDto.vehicleModelInfo.filter { model ->
            model.year == year &&
                    (engineId == null || model.engineInfo.any { it.engineId == engineId }) &&
                    (bodyId == null || model.bodyInfo.any { it.bodyId == bodyId })
        }
        val needsModel = finalMatchingModels.size > 1
        val autoSelectedModelId = if (!needsModel && finalMatchingModels.isNotEmpty()) finalMatchingModels.first().modelId else null

        Log.d(logTag, "Final Model Check: ${finalMatchingModels.size} matching models. NeedsSelection=$needsModel, AutoSelectedID=$autoSelectedModelId")
        _uiState.update {
            it.copy(
                availableModels = if (needsModel) finalMatchingModels else emptyList(),
                needsModelSelection = needsModel,
                // Check if current selected model is still valid, otherwise use auto-selected or null
                selectedModelId = if (needsModel && it.selectedModelId != null && finalMatchingModels.any { mod -> mod.modelId == it.selectedModelId }) {
                    it.selectedModelId // Keep existing valid selection if needed
                } else {
                    autoSelectedModelId // Otherwise use auto-select (which could be null)
                }
            )
        }
        // Note: updateButtonStates called by the caller (prepareDataForStep or selectModel/onMileageChange)
    }

    // --- Navigation Actions (Called from UI) ---

    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoading) {
            Log.d(logTag, "Next ignored: Step=${currentState.currentStep}, Enabled=${currentState.isNextEnabled}, Loading=${currentState.isLoading}")
            return
        }
        if (currentState.error != null) _uiState.update { it.copy(error = null) } // Clear error on proceed

        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal >= AddVehicleStep.values().size) {
            Log.w(logTag, "Already at the last step (CONFIRM).")
            return // Should be handled by Save button
        }
        val nextStep = AddVehicleStep.values()[nextStepOrdinal]

        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndProceed() // Advances step on success
        } else {
            Log.d(logTag, "Moving from ${currentState.currentStep} to $nextStep")
            _uiState.update { it.copy(currentStep = nextStep) }
            prepareDataForStep(nextStep) // Prepare data for the step we just moved TO
            updateButtonStates()        // Update buttons for the step we just moved TO
        }
    }

    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoading) return

        val previousStep = AddVehicleStep.values().last { it.ordinal < currentStep.ordinal }
        Log.d(logTag, "Moving back from $currentStep to $previousStep")

        // Reset OPTIONS/FLAGS for steps strictly after the one we are returning to
        // Keep the confirmed selections themselves
        resetOptionsStrictlyBelow(previousStep)

        _uiState.update {
            it.copy(
                currentStep = previousStep, // Go back to previous step
                error = null, // Clear transient errors
                mileageValidationError = null,
                vinValidationError = null
            )
        }
        // Reload/prepare data needed for the step we just returned to
        prepareDataForStep(previousStep)
        // Update button enablement based on the state of the step we returned to
        updateButtonStates()
    }

    // --- Helper Functions ---

    // Loads/filters data needed for the step being navigated TO
    private fun prepareDataForStep(step: AddVehicleStep) {
        Log.d(logTag, "Preparing data for step: $step")
        val currentState = _uiState.value // Get current state *after* potential step change
        when (step) {
            AddVehicleStep.SERIES -> {
                // Ensure producer/series options are available
                analyzeProducers() // This potentially filters series too if producer is auto-selected
            }
            AddVehicleStep.ENGINE -> {
                // Load engine options based on the determined year
                currentState.determinedYear?.let { year ->
                    filterEnginesForYear(year)
                } ?: run {
                    Log.e(logTag, "Cannot prepare ENGINE step: determinedYear is null.")
                    _uiState.update { it.copy(error = "Could not determine vehicle year.", isNextEnabled = false)}
                }
            }
            AddVehicleStep.BODY -> {
                // Load body options based on determined year and confirmed engine
                filterBodiesForEngine(currentState.confirmedEngine?.engineId)
            }
            AddVehicleStep.MILEAGE -> {
                // Check if final model disambiguation is needed
                checkFinalModelDisambiguation(currentState.confirmedBody?.bodyId)
            }
            AddVehicleStep.VIN, AddVehicleStep.CONFIRM -> { /* No dynamic data prep needed */ }
        }
    }

    // Resets AVAILABLE OPTIONS and FLAGS for steps STRICTLY AFTER the target step.
    // Keeps confirmed selections for ALL steps up to and including the target step.
    private fun resetOptionsStrictlyBelow(targetStep: AddVehicleStep) {
        _uiState.update { currentState ->
            var updated = currentState
            // Reset Engine OPTIONS if going back TO Series or VIN
            if (targetStep < AddVehicleStep.ENGINE) {
                updated = updated.copy(availableEngines = emptyList(), needsEngineConfirmation = false)
            }
            // Reset Body OPTIONS if going back before Body step
            if (targetStep < AddVehicleStep.BODY) {
                updated = updated.copy(availableBodies = emptyList(), needsBodyConfirmation = false)
            }
            // Reset Mileage INPUT/VALIDATION and Final Model OPTIONS if going back before Mileage step
            if (targetStep < AddVehicleStep.MILEAGE) {
                updated = updated.copy(
                    mileageInput = "", mileageValidationError = null, // Reset input
                    availableModels = emptyList(), needsModelSelection = false // Reset model options/flag
                    // Keep selectedModelId if it was chosen
                )
            }
            // Always clear save success flag when navigating back
            updated = updated.copy(isSaveSuccess = false)
            updated
        }
        Log.d(logTag, "Reset options STRICTLY below step: $targetStep")
    }

    // Updates the enabled state of the Next/Previous buttons based on the *current* state
    private fun updateButtonStates() {
        _uiState.update { state ->
            val isPrevEnabled = state.currentStep != AddVehicleStep.VIN && !state.isLoading
            var isNextEnabled = !state.isLoading && state.error == null // Basic checks

            // Check requirements of the CURRENT step to potentially disable Next
            when (state.currentStep) {
                AddVehicleStep.VIN -> isNextEnabled = isNextEnabled && state.vinInput.length == 17 && state.vinValidationError == null
                AddVehicleStep.SERIES -> isNextEnabled = isNextEnabled && state.selectedProducer != null && (!state.needsSeriesSelection || state.selectedSeriesDto != null) && state.determinedYear != null // Year MUST be determined
                AddVehicleStep.ENGINE -> isNextEnabled = isNextEnabled && (!state.needsEngineConfirmation || state.confirmedEngine != null)
                AddVehicleStep.BODY -> isNextEnabled = isNextEnabled && (!state.needsBodyConfirmation || state.confirmedBody != null)
                AddVehicleStep.MILEAGE -> isNextEnabled = isNextEnabled && state.mileageInput.isNotBlank() && state.mileageValidationError == null && state.mileageInput.toLongOrNull() != null && (!state.needsModelSelection || state.selectedModelId != null)
                AddVehicleStep.CONFIRM -> isNextEnabled = false // Next button is never active on Confirm (it's the Save button)
            }

            state.copy(
                isPreviousEnabled = isPrevEnabled,
                isNextEnabled = isNextEnabled
                // Save button enablement is handled directly in the UI based on isLoading and currentStep
            )
        }
        // Log button states after update for debugging
        Log.d(logTag, "Button States Updated: Prev=${_uiState.value.isPreviousEnabled}, Next=${_uiState.value.isNextEnabled} for Step=${_uiState.value.currentStep}")
    }


    // --- Step 6: Confirmation & Save Action ---

    fun saveVehicle() {
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isLoading) {
            Log.w(logTag, "Save ignored: Not on Confirm step or already loading.")
            return
        }

        val state = _uiState.value
        // Gather data for saving - use determinedYear
        val year = state.determinedYear
        val mileage = state.mileageInput.toLongOrNull()
        val finalModelId = state.selectedModelId // May be null
        val confirmedEngineId = state.confirmedEngine?.engineId
        val confirmedBodyId = state.confirmedBody?.bodyId
        val producer = state.selectedProducer
        val seriesName = state.selectedSeriesDto?.seriesName

        // --- Validation before saving ---
        var validationError: String? = null
        if (producer == null || seriesName == null) validationError = "Make/Series information missing."
        else if (year == null) validationError = "Could not determine vehicle year."
        else if (mileage == null || mileage < 0) validationError = "Invalid mileage entered."
        else if (state.needsEngineConfirmation && confirmedEngineId == null) validationError = "Engine details selection required but missing."
        else if (state.needsBodyConfirmation && confirmedBodyId == null) validationError = "Body style selection required but missing."
        else if (state.needsModelSelection && finalModelId == null) validationError = "Specific model selection required but missing."
        // Add any other crucial checks

        if (validationError != null) {
            _uiState.update { it.copy(error = "Cannot save: $validationError") }
            Log.e(logTag, "Save validation failed: $validationError")
            return
        }

        // --- Proceed with Save ---
        _uiState.update { it.copy(isLoading = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }
        Log.d(logTag, "--- Attempting to Save Vehicle ---")
        Log.d(logTag, " VIN: ${state.vinInput}")
        Log.d(logTag, " Producer: $producer")
        Log.d(logTag, " Series: $seriesName")
        Log.d(logTag, " Year: $year")
        Log.d(logTag, " Engine ID: $confirmedEngineId")
        Log.d(logTag, " Body ID: $confirmedBodyId")
        Log.d(logTag, " Final Model ID: $finalModelId")
        Log.d(logTag, " Mileage: $mileage")
        Log.d(logTag, "---------------------------------")

        viewModelScope.launch {
            // TODO: Construct the actual Save Request DTO
            // val saveRequest = VehicleSaveRequest(...)

            // TODO: Replace with actual call to saveVehicleRepository.saveVehicle(saveRequest)
            kotlinx.coroutines.delay(1500) // Simulate network
            val success = true // Simulate result

            // --- Handle Save Result ---
            if (success) { // Replace with actual result check (e.g., result.isSuccess)
                Log.i(logTag, "Vehicle saved successfully.")
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) } // Trigger navigation via UI effect
            } else { // Replace with actual result check (e.g., result.isFailure)
                val saveErrorMsg = "Failed to save vehicle (Simulated)." // Replace with result.exceptionOrNull()?.message
                Log.e(logTag, "Save failed: $saveErrorMsg")
                // Update state: stop loading, show error, re-enable previous button
                _uiState.update { it.copy(isLoading = false, error = saveErrorMsg, isPreviousEnabled = true) }
            }
        }
    }

    // --- UI Interaction Helpers ---

    // Called by UI after error message has been shown
    fun errorShown() {
        // Clear the error message from the state if we are not in a loading state
        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(error = null) }
        }
    }

    // Called by UI after successful save navigation has occurred
    fun saveSuccessNavigationComplete() {
        // Clear the success flag to prevent re-navigation on recomposition
        _uiState.update { it.copy(isSaveSuccess = false) }
        // Optional: Reset the entire state back to the initial VIN entry screen
        // _uiState.value = AddVehicleUiState()
    }
}