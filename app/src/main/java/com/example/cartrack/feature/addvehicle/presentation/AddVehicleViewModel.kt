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

    // --- MANIPULARE EVENIMENTE UI ---

    fun onVinInputChange(newVin: String) {
        _uiState.update { it.copy(
            vinInput = newVin.filter { it.isLetterOrDigit() }.uppercase().take(17),
            vinValidationError = null,
            hasAttemptedNext = false
        )}
        updateButtonStates()
    }

    fun selectSeriesAndYear(seriesName: String, year: Int) {
        _uiState.update { it.copy(
            selectedSeriesName = seriesName,
            selectedYear = year,
            hasAttemptedNext = false,
            // Reset fields for subsequent steps
            availableEngineSizes = emptyList(), selectedEngineSize = null,
            availableEngineTypes = emptyList(), selectedEngineType = null,
            availableTransmissions = emptyList(), selectedTransmission = null,
            availableDriveTypes = emptyList(), selectedDriveType = null,
            confirmedEngineId = null,
            availableBodyTypes = emptyList(), selectedBodyType = null,
            availableDoorNumbers = emptyList(), selectedDoorNumber = null,
            availableSeatNumbers = emptyList(), selectedSeatNumber = null,
            confirmedBodyId = null,
            determinedModelId = null
        )}
        prepareDataForStep(AddVehicleStep.ENGINE_DETAILS)
        updateButtonStates()
    }

    fun selectEngineSize(size: Double?) {
        _uiState.update { it.copy(
            selectedEngineSize = size, hasAttemptedNext = false,
            availableEngineTypes = emptyList(), selectedEngineType = null,
            availableTransmissions = emptyList(), selectedTransmission = null,
            availableDriveTypes = emptyList(), selectedDriveType = null,
            confirmedEngineId = null
        )}
        if (size != null) filterEngineTypes()
        updateButtonStates()
    }

    fun selectEngineType(type: String?) {
        _uiState.update { it.copy(
            selectedEngineType = type, hasAttemptedNext = false,
            availableTransmissions = emptyList(), selectedTransmission = null,
            availableDriveTypes = emptyList(), selectedDriveType = null,
            confirmedEngineId = null
        )}
        if (type != null) filterTransmissions()
        updateButtonStates()
    }

    fun selectTransmission(transmission: String?) {
        _uiState.update { it.copy(
            selectedTransmission = transmission, hasAttemptedNext = false,
            availableDriveTypes = emptyList(), selectedDriveType = null,
            confirmedEngineId = null
        )}
        if (transmission != null) filterDriveTypes()
        updateButtonStates()
    }

    fun selectDriveType(driveType: String?) {
        _uiState.update { it.copy(
            selectedDriveType = driveType,
            confirmedEngineId = null,
            hasAttemptedNext = false
        )}
        determineConfirmedEngineId()
        updateButtonStates()
    }

    fun selectBodyType(type: String?) {
        _uiState.update { it.copy(
            selectedBodyType = type, hasAttemptedNext = false,
            availableDoorNumbers = emptyList(), selectedDoorNumber = null,
            availableSeatNumbers = emptyList(), selectedSeatNumber = null,
            confirmedBodyId = null
        )}
        if (type != null) filterDoorNumbers()
        updateButtonStates()
    }

    fun selectDoorNumber(doors: Int?) {
        _uiState.update { it.copy(
            selectedDoorNumber = doors, hasAttemptedNext = false,
            availableSeatNumbers = emptyList(), selectedSeatNumber = null,
            confirmedBodyId = null
        )}
        if (doors != null) filterSeatNumbers()
        updateButtonStates()
    }

    fun selectSeatNumber(seats: Int?) {
        _uiState.update { it.copy(
            selectedSeatNumber = seats,
            confirmedBodyId = null,
            hasAttemptedNext = false
        )}
        determineConfirmedBodyId()
        updateButtonStates()
    }

    fun onMileageChange(mileage: String) {
        val digitsOnly = mileage.filter { it.isDigit() }.take(9)
        val isValid = digitsOnly.isNotBlank() && digitsOnly.toLongOrNull()?.let { it >= 0 } ?: false
        _uiState.update { it.copy(
            mileageInput = digitsOnly,
            mileageValidationError = if (digitsOnly.isNotEmpty() && !isValid) "Invalid mileage" else null,
            hasAttemptedNext = false
        )}
        updateButtonStates()
    }

    fun userClickedSkipVinOrEnterManually() {
        Log.d(logTag, "User chose to skip VIN. Moving to SERIES_YEAR for manual entry.")
        _uiState.update {
            it.copy(
                currentStep = AddVehicleStep.SERIES_YEAR,
                allDecodedOptions = emptyList(),
                vinValidationError = null,
                isLoadingVinDetails = false,
                hasAttemptedNext = false,
                // Reset all selections
                selectedSeriesName = null, selectedYear = null,
                selectedEngineSize = null, selectedEngineType = null, selectedTransmission = null, selectedDriveType = null, confirmedEngineId = null,
                selectedBodyType = null, selectedDoorNumber = null, selectedSeatNumber = null, confirmedBodyId = null,
                determinedModelId = null
            )
        }
        prepareDataForStep(AddVehicleStep.SERIES_YEAR)
        updateButtonStates()
    }


    // --- NAVIGARE ---

    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoadingVinDetails || currentState.isSaving || currentState.isLoadingNextStep) return

        _uiState.update { it.copy(hasAttemptedNext = true) }

        if (!isCurrentStepValid(currentState)) {
            Log.w(logTag, "goToNextStep validation failed for ${currentState.currentStep}")
            updateButtonStates()
            return
        }

        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndDecideNextStep()
            return
        }

        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal >= AddVehicleStep.values().size) return
        val nextStep = AddVehicleStep.values()[nextStepOrdinal]

        _uiState.update { it.copy(currentStep = nextStep, isLoadingNextStep = true, hasAttemptedNext = false) }
        viewModelScope.launch {
            prepareDataForStep(nextStep)
            _uiState.update { it.copy(isLoadingNextStep = false) }
            updateButtonStates()
        }
    }

    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoadingVinDetails || _uiState.value.isSaving || _uiState.value.isLoadingNextStep) return
        val previousStepOrdinal = currentStep.ordinal - 1
        if (previousStepOrdinal < 0) return
        val previousStep = AddVehicleStep.values()[previousStepOrdinal]

        _uiState.update { it.copy(
            currentStep = previousStep, error = null, vinValidationError = null,
            mileageValidationError = null, hasAttemptedNext = false
        )}
        prepareDataForStep(previousStep)
        updateButtonStates()
    }

    // --- LOGICA INTERNĂ ---

    private fun decodeVinAndDecideNextStep() {
        _uiState.update { it.copy(isLoadingVinDetails = true, error = null) }
        viewModelScope.launch {
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoadingVinDetails = false, error = "Cannot identify user.") }
                updateButtonStates()
                return@launch
            }

            val result = vinDecoderRepository.decodeVin(_uiState.value.vinInput, clientId)
            result.onSuccess { decodedInfoList ->
                if (decodedInfoList.isEmpty()) {
                    _uiState.update { it.copy(isLoadingVinDetails = false, vinValidationError = "VIN not found in database.") }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingVinDetails = false,
                            currentStep = AddVehicleStep.SERIES_YEAR,
                            allDecodedOptions = decodedInfoList,
                            vinValidationError = null,
                            hasAttemptedNext = false
                        )
                    }
                    prepareDataForStep(AddVehicleStep.SERIES_YEAR)
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoadingVinDetails = false,
                        vinValidationError = exception.message ?: "Invalid VIN. Please check and retry."
                    )
                }
            }
            updateButtonStates()
        }
    }

    private fun prepareDataForStep(step: AddVehicleStep) {
        when (step) {
            AddVehicleStep.SERIES_YEAR -> filterSeriesAndYears()
            AddVehicleStep.ENGINE_DETAILS -> {
                filterEngineSizes()
                if (_uiState.value.selectedEngineSize != null) filterEngineTypes()
                if (_uiState.value.selectedEngineType != null) filterTransmissions()
                if (_uiState.value.selectedTransmission != null) filterDriveTypes()
            }
            AddVehicleStep.BODY_DETAILS -> {
                filterBodyTypes()
                if (_uiState.value.selectedBodyType != null) filterDoorNumbers()
                if (_uiState.value.selectedDoorNumber != null) filterSeatNumbers()
            }
            AddVehicleStep.VEHICLE_INFO -> determineModelId()
            else -> {}
        }
    }

    private fun isCurrentStepValid(state: AddVehicleUiState): Boolean {
        return when (state.currentStep) {
            AddVehicleStep.VIN -> state.vinInput.length == 17 && state.vinValidationError == null
            AddVehicleStep.SERIES_YEAR -> state.selectedSeriesName != null && state.selectedYear != null
            AddVehicleStep.ENGINE_DETAILS -> state.confirmedEngineId != null
            AddVehicleStep.BODY_DETAILS -> state.confirmedBodyId != null
            AddVehicleStep.VEHICLE_INFO -> state.mileageInput.isNotBlank() && state.mileageValidationError == null && state.determinedModelId != null
            AddVehicleStep.CONFIRM -> true
        }
    }

    private fun updateButtonStates() {
        _uiState.update { state ->
            val commonLoading = state.isLoadingVinDetails || state.isSaving || state.isLoadingNextStep
            val isPrevEnabled = state.currentStep.ordinal > AddVehicleStep.VIN.ordinal && !commonLoading
            val isNextEnabled = isCurrentStepValid(state) && !commonLoading && state.currentStep != AddVehicleStep.CONFIRM

            state.copy(isPreviousEnabled = isPrevEnabled, isNextEnabled = isNextEnabled)
        }
    }

    private fun getFilteredModels(
        seriesNameFilter: String? = _uiState.value.selectedSeriesName,
        yearFilter: Int? = _uiState.value.selectedYear
    ): List<ModelDecodedDto> {
        if (seriesNameFilter == null || yearFilter == null) return emptyList()

        val producerOfSelectedSeries = _uiState.value.allDecodedOptions.firstOrNull { opt -> seriesNameFilter.startsWith(opt.producer) && seriesNameFilter.endsWith(opt.seriesName) }?.producer
        val actualSeriesName = producerOfSelectedSeries?.let { seriesNameFilter.removePrefix("$it ") } ?: seriesNameFilter

        return _uiState.value.allDecodedOptions
            .filter { dto -> dto.producer == producerOfSelectedSeries && dto.seriesName == actualSeriesName }
            .flatMap { it.vehicleModelInfo }
            .filter { model -> model.year == yearFilter }
    }

    private fun filterSeriesAndYears() {
        val uniqueSeriesYears = _uiState.value.allDecodedOptions
            .flatMap { dto -> dto.vehicleModelInfo.map { modelInfo -> dto.producer to (dto.seriesName to modelInfo.year) } }
            .map { (producer, seriesYearPair) -> "$producer ${seriesYearPair.first}" to seriesYearPair.second }
            .distinct().sortedWith(compareBy({ it.first }, { it.second }))
        _uiState.update { it.copy(availableSeriesAndYears = uniqueSeriesYears) }
    }

    private fun filterEngineSizes() {
        _uiState.update { it.copy(availableEngineSizes = getFilteredModels().flatMap { it.engineInfo }.map { it.size }.distinct().sorted()) }
    }

    private fun filterEngineTypes() {
        _uiState.update { it.copy(availableEngineTypes = getFilteredModels().flatMap { it.engineInfo }.filter { it.size == _uiState.value.selectedEngineSize }.map { it.engineType }.distinct().sorted()) }
    }

    private fun filterTransmissions() {
        _uiState.update { it.copy(availableTransmissions = getFilteredModels().flatMap { it.engineInfo }.filter { it.size == _uiState.value.selectedEngineSize && it.engineType == _uiState.value.selectedEngineType }.map { it.transmission }.distinct().sorted()) }
    }

    private fun filterDriveTypes() {
        _uiState.update { it.copy(availableDriveTypes = getFilteredModels().flatMap { it.engineInfo }.filter { it.size == _uiState.value.selectedEngineSize && it.engineType == _uiState.value.selectedEngineType && it.transmission == _uiState.value.selectedTransmission }.map { it.driveType }.distinct().sorted()) }
    }

    private fun determineConfirmedEngineId() {
        val state = _uiState.value
        val confirmedEngine = getFilteredModels().flatMap { it.engineInfo }.find { it.size == state.selectedEngineSize && it.engineType == state.selectedEngineType && it.transmission == state.selectedTransmission && it.driveType == state.selectedDriveType }
        _uiState.update { it.copy(confirmedEngineId = confirmedEngine?.engineId) }
    }

    private fun filterBodyTypes() {
        val models = getFilteredModels().filter { it.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableBodyTypes = models.flatMap { it.bodyInfo }.map { it.bodyType }.distinct().sorted()) }
    }

    private fun filterDoorNumbers() {
        val models = getFilteredModels().filter { it.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableDoorNumbers = models.flatMap { it.bodyInfo }.filter { it.bodyType == _uiState.value.selectedBodyType }.map { it.doorNumber }.distinct().sorted()) }
    }

    private fun filterSeatNumbers() {
        val models = getFilteredModels().filter { it.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableSeatNumbers = models.flatMap { it.bodyInfo }.filter { it.bodyType == _uiState.value.selectedBodyType && it.doorNumber == _uiState.value.selectedDoorNumber }.map { it.seatNumber }.distinct().sorted()) }
    }

    private fun determineConfirmedBodyId() {
        val state = _uiState.value
        val models = getFilteredModels().filter { it.engineInfo.any { e -> e.engineId == state.confirmedEngineId } }
        val confirmedBody = models.flatMap { it.bodyInfo }.find { it.bodyType == state.selectedBodyType && it.doorNumber == state.selectedDoorNumber && it.seatNumber == state.selectedSeatNumber }
        _uiState.update { it.copy(confirmedBodyId = confirmedBody?.bodyId) }
    }

    private fun determineModelId() {
        val state = _uiState.value
        if (state.confirmedEngineId == null || state.confirmedBodyId == null) {
            _uiState.update { it.copy(determinedModelId = null) }
            return
        }
        val finalModel = getFilteredModels().find { model ->
            model.engineInfo.any { it.engineId == state.confirmedEngineId } &&
                    model.bodyInfo.any { it.bodyId == state.confirmedBodyId }
        }
        _uiState.update { it.copy(determinedModelId = finalModel?.modelId) }
    }

    // --- SALVARE ---

    fun saveVehicle() {
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isSaving) return

        val state = _uiState.value
        viewModelScope.launch {
            // Validări rapide înainte de a trimite cererea
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(error = "Cannot save: User not identified.") }
                return@launch
            }
            if (state.determinedModelId == null) {
                _uiState.update { it.copy(error = "Cannot save: Vehicle model is not fully specified.") }
                return@launch
            }
            if (state.mileageInput.isBlank() || state.mileageInput.toDoubleOrNull() == null) {
                _uiState.update { it.copy(error = "Cannot save: Mileage is invalid.") }
                return@launch
            }

            val saveRequest = VehicleSaveRequestDto(
                clientId = clientId,
                modelId = state.determinedModelId,
                vin = state.vinInput.ifBlank { "VIN_NOT_PROVIDED_${System.currentTimeMillis()}" },
                mileage = state.mileageInput.toDouble()
            )

            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = saveVehicleRepository.saveVehicle(saveRequest)
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isSaving = false, error = exception.message ?: "Failed to save vehicle.") }
            }
        }
    }

    fun resetSaveStatus() { _uiState.update { it.copy(isSaveSuccess = false, error = null) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}