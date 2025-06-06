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
    private val saveVehicleRepository: SaveVehicleRepository,
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    private val logTag = "AddVehicleVM"

    // --- LOGICA MODIFICATĂ ESTE ÎN `decodeVinAndDecideNextStep` ---

    private fun decodeVinAndDecideNextStep() {
        val vin = _uiState.value.vinInput
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be 17 characters.") }
            updateButtonStates()
            return
        }

        _uiState.update { it.copy(vinValidationError = null, isLoadingVinDetails = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

        viewModelScope.launch {
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                // Afișează eroarea generală dacă nu se poate identifica utilizatorul
                _uiState.update { it.copy(isLoadingVinDetails = false, error = "Cannot identify user.") }
                updateButtonStates()
                return@launch
            }

            Log.d(logTag, "Decoding VIN: $vin for client ID: $clientId")
            val result = vinDecoderRepository.decodeVin(vin, clientId)

            result.onSuccess { decodedInfoList ->
                Log.d(logTag, "VIN Decode Success: ${decodedInfoList.size} options found.")
                if (decodedInfoList.isEmpty()) {
                    // SUCCES, DAR LISTA E GOALĂ -> EROARE DE VALIDARE PENTRU VIN
                    _uiState.update {
                        it.copy(
                            isLoadingVinDetails = false,
                            vinValidationError = "VIN is valid but not found in our database. Try another or enter details manually.",
                            allDecodedOptions = emptyList()
                        )
                    }
                } else {
                    // SUCCES REAL -> MERGI LA PASUL URMĂTOR
                    _uiState.update {
                        it.copy(
                            isLoadingVinDetails = false,
                            currentStep = AddVehicleStep.SERIES_YEAR, // Treci la pasul următor
                            allDecodedOptions = decodedInfoList,
                            // Resetează selecțiile pentru noul set de opțiuni
                            selectedSeriesName = null, selectedYear = null,
                            selectedEngineSize = null, selectedEngineType = null, selectedTransmission = null, selectedDriveType = null, confirmedEngineId = null,
                            selectedBodyType = null, selectedDoorNumber = null, selectedSeatNumber = null, confirmedBodyId = null,
                            determinedModelId = null
                        )
                    }
                    prepareDataForStep(AddVehicleStep.SERIES_YEAR)
                }
            }.onFailure { exception ->
                // EȘEC TOTAL -> EROARE DE VALIDARE PENTRU VIN
                Log.e(logTag, "VIN Decode Failed", exception)
                _uiState.update {
                    it.copy(
                        isLoadingVinDetails = false,
                        vinValidationError = exception.message ?: "Invalid or non-existent VIN. Please check and try again.",
                        allDecodedOptions = emptyList()
                    )
                }
            }
            // După ce s-a terminat procesarea (fie succes, fie eșec), actualizează starea butoanelor.
            updateButtonStates()
        }
    }

    // --- RESTUL FIȘIERULUI RĂMÂNE NEMODIFICAT ---
    // (Funcțiile de selectare, goToNextStep, goToPreviousStep, filtrare etc. rămân la fel)

    fun onVinInputChange(newVin: String) {
        if (_uiState.value.currentStep != AddVehicleStep.VIN) return
        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase().take(17)
        _uiState.update {
            it.copy(
                vinInput = processedVin,
                // Resetează eroarea de validare la fiecare tastare
                vinValidationError = null,
                error = null
            )
        }
        updateButtonStates()
    }

    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoadingVinDetails || currentState.isSaving || currentState.isLoadingNextStep) return
        _uiState.update { it.copy(error = null) }

        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndDecideNextStep()
            return
        }

        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal >= AddVehicleStep.values().size) return

        val nextStep = AddVehicleStep.values()[nextStepOrdinal]
        Log.d(logTag, "Moving from ${currentState.currentStep} to $nextStep")

        _uiState.update { it.copy(currentStep = nextStep, isLoadingNextStep = true) }
        viewModelScope.launch {
            prepareDataForStep(nextStep)
            _uiState.update { it.copy(isLoadingNextStep = false) }
            updateButtonStates()
        }
    }

    // Funcțiile de selectare (selectSeriesAndYear, selectEngineSize etc.) rămân la fel
    fun selectSeriesAndYear(seriesName: String, year: Int) {
        Log.d(logTag, "Series '$seriesName' and Year '$year' selected.")
        _uiState.update {
            it.copy(
                selectedSeriesName = seriesName,
                selectedYear = year,
                selectedEngineSize = null, availableEngineTypes = emptyList(), selectedEngineType = null,
                availableTransmissions = emptyList(), selectedTransmission = null,
                availableDriveTypes = emptyList(), selectedDriveType = null, confirmedEngineId = null,
                availableBodyTypes = emptyList(), selectedBodyType = null,
                availableDoorNumbers = emptyList(), selectedDoorNumber = null,
                availableSeatNumbers = emptyList(), selectedSeatNumber = null,
                confirmedBodyId = null,
                determinedModelId = null, error = null
            )
        }
        prepareDataForStep(AddVehicleStep.ENGINE_DETAILS)
        updateButtonStates()
    }

    fun selectEngineSize(size: Double?) {
        _uiState.update {
            it.copy(
                selectedEngineSize = size,
                availableEngineTypes = emptyList(), selectedEngineType = null,
                availableTransmissions = emptyList(), selectedTransmission = null,
                availableDriveTypes = emptyList(), selectedDriveType = null,
                confirmedEngineId = null, error = null
            )
        }
        if (size != null) filterEngineTypes() else _uiState.update { it.copy(availableEngineTypes = emptyList()) }
        updateButtonStates()
    }

    fun selectEngineType(type: String?) {
        _uiState.update {
            it.copy(
                selectedEngineType = type,
                availableTransmissions = emptyList(), selectedTransmission = null,
                availableDriveTypes = emptyList(), selectedDriveType = null,
                confirmedEngineId = null, error = null
            )
        }
        if (type != null) filterTransmissions() else _uiState.update { it.copy(availableTransmissions = emptyList()) }
        updateButtonStates()
    }

    fun selectTransmission(transmission: String?) {
        _uiState.update {
            it.copy(
                selectedTransmission = transmission,
                availableDriveTypes = emptyList(), selectedDriveType = null,
                confirmedEngineId = null, error = null
            )
        }
        if (transmission != null) filterDriveTypes() else _uiState.update { it.copy(availableDriveTypes = emptyList()) }
        updateButtonStates()
    }

    fun selectDriveType(driveType: String?) {
        _uiState.update { it.copy(selectedDriveType = driveType, confirmedEngineId = null, error = null) }
        determineConfirmedEngineId()
        updateButtonStates()
    }

    fun selectBodyType(type: String?) {
        _uiState.update {
            it.copy(
                selectedBodyType = type,
                availableDoorNumbers = emptyList(), selectedDoorNumber = null,
                availableSeatNumbers = emptyList(), selectedSeatNumber = null,
                confirmedBodyId = null, error = null
            )
        }
        if (type != null) filterDoorNumbers() else _uiState.update { it.copy(availableDoorNumbers = emptyList()) }
        updateButtonStates()
    }

    fun selectDoorNumber(doors: Int?) {
        _uiState.update {
            it.copy(
                selectedDoorNumber = doors,
                availableSeatNumbers = emptyList(), selectedSeatNumber = null,
                confirmedBodyId = null, error = null
            )
        }
        if (doors != null) filterSeatNumbers() else _uiState.update { it.copy(availableSeatNumbers = emptyList()) }
        updateButtonStates()
    }

    fun selectSeatNumber(seats: Int?) {
        _uiState.update { it.copy(selectedSeatNumber = seats, confirmedBodyId = null, error = null) }
        determineConfirmedBodyId()
        updateButtonStates()
    }

    fun onMileageChange(mileage: String) {
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

    fun userClickedSkipVinOrEnterManually() {
        Log.d(logTag, "User chose to skip VIN or enter manually. Moving to SERIES_YEAR.")
        _uiState.update {
            it.copy(
                currentStep = AddVehicleStep.SERIES_YEAR,
                allDecodedOptions = emptyList(),
                vinInput = it.vinInput,
                vinValidationError = null,
                isLoadingVinDetails = false,
                selectedSeriesName = null, selectedYear = null, availableSeriesAndYears = emptyList(),
                selectedEngineSize = null, availableEngineTypes = emptyList(), selectedEngineType = null,
                availableTransmissions = emptyList(), selectedTransmission = null,
                availableDriveTypes = emptyList(), selectedDriveType = null, confirmedEngineId = null,
                availableBodyTypes = emptyList(), selectedBodyType = null,
                availableDoorNumbers = emptyList(), selectedDoorNumber = null,
                availableSeatNumbers = emptyList(), selectedSeatNumber = null,
                confirmedBodyId = null,
                determinedModelId = null, error = null, isLoadingNextStep = false
            )
        }
        prepareDataForStep(AddVehicleStep.SERIES_YEAR)
        updateButtonStates()
    }

    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoadingVinDetails || _uiState.value.isSaving || _uiState.value.isLoadingNextStep) return

        val previousStepOrdinal = currentStep.ordinal - 1
        if (previousStepOrdinal < 0) return

        val previousStep = AddVehicleStep.values()[previousStepOrdinal]
        Log.d(logTag, "Moving back from $currentStep to $previousStep")
        _uiState.update { it.copy(currentStep = previousStep, error = null, mileageValidationError = null, vinValidationError = null) }
        prepareDataForStep(previousStep)
        updateButtonStates()
    }

    private fun prepareDataForStep(step: AddVehicleStep) {
        Log.d(logTag, "Preparing data for step: $step")
        when (step) {
            AddVehicleStep.SERIES_YEAR -> filterSeriesAndYears()
            AddVehicleStep.ENGINE_DETAILS -> filterEngineSizes()
            AddVehicleStep.BODY_DETAILS -> filterBodyTypes()
            AddVehicleStep.VEHICLE_INFO -> determineModelId()
            else -> { /* No specific data prep */ }
        }
    }

    // Funcțiile de filtrare... (le las aici pentru completitudine, nu se schimbă)
    private fun filterSeriesAndYears() {
        val decodedOptions = _uiState.value.allDecodedOptions
        val uniqueSeriesYears = if (decodedOptions.isNotEmpty()) {
            decodedOptions
                .flatMap { dto -> dto.vehicleModelInfo.map { modelInfo -> dto.producer to (dto.seriesName to modelInfo.year) } }
                .map { (producer, seriesYearPair) -> "$producer ${seriesYearPair.first}" to seriesYearPair.second }
                .distinct()
                .sortedWith(compareBy({ it.first }, { it.second }))
        } else { emptyList() }
        _uiState.update { it.copy(availableSeriesAndYears = uniqueSeriesYears, selectedSeriesName = null, selectedYear = null) }
    }

    private fun getFilteredModels(
        seriesNameFilter: String? = _uiState.value.selectedSeriesName,
        yearFilter: Int? = _uiState.value.selectedYear,
        engineIdFilter: Int? = _uiState.value.confirmedEngineId,
        bodyIdFilter: Int? = _uiState.value.confirmedBodyId
    ): List<ModelDecodedDto> {
        if (seriesNameFilter == null || yearFilter == null) return emptyList()
        val producerOfSelectedSeries = _uiState.value.allDecodedOptions
            .firstOrNull { opt -> seriesNameFilter.startsWith(opt.producer) && seriesNameFilter.endsWith(opt.seriesName) }?.producer
        val actualSeriesName = producerOfSelectedSeries?.let { seriesNameFilter.removePrefix("$it ") } ?: seriesNameFilter

        return _uiState.value.allDecodedOptions
            .filter { dto -> dto.producer == producerOfSelectedSeries && dto.seriesName == actualSeriesName }
            .flatMap { it.vehicleModelInfo }
            .filter { it.year == yearFilter }
            .filter { engineIdFilter == null || it.engineInfo.any { e -> e.engineId == engineIdFilter } }
            .filter { bodyIdFilter == null || it.bodyInfo.any { b -> b.bodyId == bodyIdFilter } }
    }

    private fun filterEngineSizes() {
        val models = getFilteredModels()
        val relevantSizes = models.flatMap { it.engineInfo }.mapNotNull { it.size }.distinct().sorted()
        _uiState.update { it.copy(availableEngineSizes = relevantSizes, selectedEngineSize = null, confirmedEngineId = null) }
    }

    private fun filterEngineTypes() {
        val size = _uiState.value.selectedEngineSize ?: return
        val models = getFilteredModels()
        val relevantTypes = models.flatMap { it.engineInfo }.filter { it.size == size }.map { it.engineType }.distinct().sorted()
        _uiState.update { it.copy(availableEngineTypes = relevantTypes, selectedEngineType = null, confirmedEngineId = null) }
    }

    private fun filterTransmissions() {
        val size = _uiState.value.selectedEngineSize ?: return
        val type = _uiState.value.selectedEngineType ?: return
        val models = getFilteredModels()
        val relevantTransmissions = models.flatMap { it.engineInfo }.filter { it.size == size && it.engineType == type }.map { it.transmission }.distinct().sorted()
        _uiState.update { it.copy(availableTransmissions = relevantTransmissions, selectedTransmission = null, confirmedEngineId = null) }
    }

    private fun filterDriveTypes() {
        val size = _uiState.value.selectedEngineSize ?: return
        val type = _uiState.value.selectedEngineType ?: return
        val transmission = _uiState.value.selectedTransmission ?: return
        val models = getFilteredModels()
        val relevantDriveTypes = models.flatMap { it.engineInfo }.filter { it.size == size && it.engineType == type && it.transmission == transmission }.map { it.driveType }.distinct().sorted()
        _uiState.update { it.copy(availableDriveTypes = relevantDriveTypes, selectedDriveType = null, confirmedEngineId = null) }
    }

    private fun determineConfirmedEngineId() {
        val state = _uiState.value
        if (state.selectedEngineSize == null || state.selectedEngineType == null || state.selectedTransmission == null || state.selectedDriveType == null) {
            _uiState.update { it.copy(confirmedEngineId = null) }; return
        }
        val models = getFilteredModels()
        val confirmedEngine = models.flatMap { it.engineInfo }.find {
            it.size == state.selectedEngineSize && it.engineType == state.selectedEngineType &&
                    it.transmission == state.selectedTransmission && it.driveType == state.selectedDriveType
        }
        _uiState.update { it.copy(confirmedEngineId = confirmedEngine?.engineId) }
        Log.d(logTag, "Determined Engine ID: ${confirmedEngine?.engineId}")
    }

    private fun filterBodyTypes() {
        val models = getFilteredModels(engineIdFilter = _uiState.value.confirmedEngineId)
        if(_uiState.value.confirmedEngineId == null && _uiState.value.allDecodedOptions.isNotEmpty() && _uiState.value.currentStep > AddVehicleStep.SERIES_YEAR) {
            Log.w(logTag, "Attempting to filter body types but engine is not confirmed yet.")
            _uiState.update { it.copy(availableBodyTypes = emptyList(), selectedBodyType = null, confirmedBodyId = null) }
            return
        }
        val relevantBodyTypes = models.flatMap { it.bodyInfo }.map { it.bodyType }.distinct().sorted()
        _uiState.update { it.copy(availableBodyTypes = relevantBodyTypes, selectedBodyType = null, confirmedBodyId = null) }
    }

    private fun filterDoorNumbers() {
        val bodyType = _uiState.value.selectedBodyType ?: return
        val models = getFilteredModels(engineIdFilter = _uiState.value.confirmedEngineId)
        val relevantDoorNumbers = models.flatMap { it.bodyInfo }.filter { it.bodyType == bodyType }.map { it.doorNumber }.distinct().sorted()
        _uiState.update { it.copy(availableDoorNumbers = relevantDoorNumbers, selectedDoorNumber = null, confirmedBodyId = null) }
    }

    private fun filterSeatNumbers() {
        val bodyType = _uiState.value.selectedBodyType ?: return
        val doors = _uiState.value.selectedDoorNumber ?: return
        val models = getFilteredModels(engineIdFilter = _uiState.value.confirmedEngineId)
        val relevantSeatNumbers = models.flatMap { it.bodyInfo }.filter { it.bodyType == bodyType && it.doorNumber == doors }.map { it.seatNumber }.distinct().sorted()
        _uiState.update { it.copy(availableSeatNumbers = relevantSeatNumbers, selectedSeatNumber = null, confirmedBodyId = null) }
    }

    private fun determineConfirmedBodyId() {
        val state = _uiState.value
        if (state.selectedBodyType == null || state.selectedDoorNumber == null || state.selectedSeatNumber == null) {
            _uiState.update { it.copy(confirmedBodyId = null) }; return
        }
        val models = getFilteredModels(engineIdFilter = state.confirmedEngineId)
        val confirmedBody = models.flatMap { it.bodyInfo }.find {
            it.bodyType == state.selectedBodyType && it.doorNumber == state.selectedDoorNumber && it.seatNumber == state.selectedSeatNumber
        }
        _uiState.update { it.copy(confirmedBodyId = confirmedBody?.bodyId) }
        Log.d(logTag, "Determined Body ID: ${confirmedBody?.bodyId}")
    }

    private fun determineModelId() {
        val state = _uiState.value
        if (state.selectedSeriesName == null || state.selectedYear == null ||
            state.confirmedEngineId == null || state.confirmedBodyId == null) {
            _uiState.update { it.copy(determinedModelId = null, error = if (state.allDecodedOptions.isNotEmpty()) "Incomplete selections" else null ) }
            Log.w(logTag, "Cannot determine ModelID: Incomplete previous selections for VIN-decoded data.")
            updateButtonStates(); return
        }

        val producerOfSelectedSeries = state.allDecodedOptions
            .firstOrNull { opt -> state.selectedSeriesName?.let { series -> series.startsWith(opt.producer) && series.endsWith(opt.seriesName) } ?: false }?.producer
        val actualSeriesName = producerOfSelectedSeries?.let { state.selectedSeriesName?.removePrefix("$it ") } ?: state.selectedSeriesName

        val finalModel = state.allDecodedOptions
            .filter { dto -> dto.producer == producerOfSelectedSeries && dto.seriesName == actualSeriesName }
            .flatMap { it.vehicleModelInfo }
            .find { modelInfo ->
                modelInfo.year == state.selectedYear &&
                        modelInfo.engineInfo.any { it.engineId == state.confirmedEngineId } &&
                        modelInfo.bodyInfo.any { it.bodyId == state.confirmedBodyId }
            }

        if (finalModel != null) {
            _uiState.update { it.copy(determinedModelId = finalModel.modelId, error = null) }
            Log.i(logTag, "Final ModelID determined: ${finalModel.modelId}")
        } else {
            _uiState.update { it.copy(determinedModelId = null, error = "Could not uniquely identify model.") }
            Log.e(logTag, "Failed to determine unique ModelID.")
        }
        updateButtonStates()
    }

    private fun updateButtonStates() {
        _uiState.update { state ->
            val commonLoading = state.isLoadingVinDetails || state.isSaving || state.isLoadingNextStep
            val isPrevEnabled = state.currentStep.ordinal > AddVehicleStep.VIN.ordinal && !commonLoading

            var nextStepPossible = true
            when (state.currentStep) {
                // Pentru pasul VIN, 'Next' e activat doar dacă VIN-ul are 17 caractere. Eroarea se va afișa după apăsare.
                AddVehicleStep.VIN -> nextStepPossible = state.vinInput.length == 17
                AddVehicleStep.SERIES_YEAR -> nextStepPossible = state.selectedSeriesName != null && state.selectedYear != null
                AddVehicleStep.ENGINE_DETAILS -> nextStepPossible = state.confirmedEngineId != null
                AddVehicleStep.BODY_DETAILS -> nextStepPossible = state.confirmedBodyId != null
                AddVehicleStep.VEHICLE_INFO -> {
                    val mileageValid = state.mileageInput.isNotBlank() && state.mileageValidationError == null && state.mileageInput.toLongOrNull()?.let { it >= 0 } ?: false
                    nextStepPossible = mileageValid && state.determinedModelId != null
                }
                AddVehicleStep.CONFIRM -> nextStepPossible = false
            }
            val isNextEnabled = nextStepPossible && !commonLoading

            state.copy(isPreviousEnabled = isPrevEnabled, isNextEnabled = isNextEnabled)
        }
        Log.d(logTag, "Button States: Prev=${_uiState.value.isPreviousEnabled}, Next=${_uiState.value.isNextEnabled} for Step=${_uiState.value.currentStep}")
    }

    fun saveVehicle() {
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isSaving) return

        val state = _uiState.value
        viewModelScope.launch {
            val clientId = jwtDecoder.getClientIdFromToken()
            var validationError: String? = null

            when {
                clientId == null -> validationError = "User not identified."
                state.allDecodedOptions.isNotEmpty() && state.vinInput.length != 17 -> validationError = "Invalid VIN."
                state.allDecodedOptions.isEmpty() && state.vinInput.isNotBlank() && state.vinInput.length != 17 -> validationError = "VIN must be 17 chars if entered."

                state.selectedSeriesName == null -> validationError = "Vehicle series not selected/entered."
                state.selectedYear == null -> validationError = "Vehicle year not selected/entered."
                state.allDecodedOptions.isNotEmpty() && state.confirmedEngineId == null -> validationError = "Engine details not confirmed."
                state.allDecodedOptions.isNotEmpty() && state.confirmedBodyId == null -> validationError = "Body details not confirmed."
                state.determinedModelId == null && state.allDecodedOptions.isNotEmpty() -> validationError = "Could not determine specific vehicle model."
                state.mileageInput.isBlank() || state.mileageInput.toDoubleOrNull() == null || state.mileageInput.toDouble() < 0 -> validationError = "Valid mileage is required."
                else -> {}
            }

            if (validationError != null) {
                _uiState.update { it.copy(error = "Cannot save: $validationError", isPreviousEnabled = true, isSaving = false) }
                Log.e(logTag, "Save validation failed: $validationError")
                return@launch
            }

            val finalModelIdToSave = state.determinedModelId ?: 0

            val saveRequest = VehicleSaveRequestDto(
                clientId = clientId!!,
                modelId = finalModelIdToSave,
                vin = state.vinInput.ifBlank { "VIN_NOT_PROVIDED_${System.currentTimeMillis()}" },
                mileage = state.mileageInput.toDouble()
            )

            Log.d(logTag, "--- Attempting to Save Vehicle --- Request: $saveRequest")
            _uiState.update { it.copy(isSaving = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

            val result = saveVehicleRepository.saveVehicle(saveRequest)
            result.onSuccess {
                Log.i(logTag, "Vehicle saved successfully.")
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            }.onFailure { exception ->
                val saveErrorMsg = exception.message ?: "Failed to save vehicle."
                Log.e(logTag, "Save failed", exception)
                _uiState.update { it.copy(isSaving = false, error = saveErrorMsg, isPreviousEnabled = true) }
            }
        }
    }

    fun resetSaveStatus() { _uiState.update { it.copy(isSaveSuccess = false, error = null) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}