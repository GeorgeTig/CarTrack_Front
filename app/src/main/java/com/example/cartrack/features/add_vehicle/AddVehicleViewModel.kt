package com.example.cartrack.features.add_vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.vehicle.VehicleSaveRequestDto
import com.example.cartrack.core.data.model.vin.ModelDecodedDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.domain.repository.VinDecoderRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vinDecoderRepository: VinDecoderRepository,
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    // --- Event Handlers ---

    fun onVinInputChange(newVin: String) {
        _uiState.update {
            it.copy(
                vinInput = newVin.filter { c -> c.isLetterOrDigit() }.uppercase().take(17),
                vinValidationError = null,
                hasAttemptedNext = false
            )
        }
        updateButtonStates()
    }

    fun onMileageChange(mileage: String) {
        val digitsOnly = mileage.filter { it.isDigit() }.take(7)
        _uiState.update { it.copy(mileageInput = digitsOnly, mileageValidationError = null, hasAttemptedNext = false) }
        updateButtonStates()
    }

    fun selectSeriesAndYear(seriesName: String, year: Int) {
        _uiState.update {
            it.copy(
                selectedSeriesName = seriesName,
                selectedYear = year,
                hasAttemptedNext = false,
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
            )
        }
        prepareDataForStep(AddVehicleStep.ENGINE_DETAILS)
        updateButtonStates()
    }

    fun selectEngineSize(size: Double?) {
        _uiState.update { it.copy(selectedEngineSize = size, hasAttemptedNext = false, availableEngineTypes = emptyList(), selectedEngineType = null, availableTransmissions = emptyList(), selectedTransmission = null, availableDriveTypes = emptyList(), selectedDriveType = null, confirmedEngineId = null) }
        if (size != null) filterEngineTypes()
        updateButtonStates()
    }

    fun selectEngineType(type: String?) {
        _uiState.update { it.copy(selectedEngineType = type, hasAttemptedNext = false, availableTransmissions = emptyList(), selectedTransmission = null, availableDriveTypes = emptyList(), selectedDriveType = null, confirmedEngineId = null) }
        if (type != null) filterTransmissions()
        updateButtonStates()
    }

    fun selectTransmission(transmission: String?) {
        _uiState.update { it.copy(selectedTransmission = transmission, hasAttemptedNext = false, availableDriveTypes = emptyList(), selectedDriveType = null, confirmedEngineId = null) }
        if (transmission != null) filterDriveTypes()
        updateButtonStates()
    }

    fun selectDriveType(driveType: String?) {
        _uiState.update { it.copy(selectedDriveType = driveType, confirmedEngineId = null, hasAttemptedNext = false) }
        determineConfirmedEngineId()
        updateButtonStates()
    }

    fun selectBodyType(type: String?) {
        _uiState.update { it.copy(selectedBodyType = type, hasAttemptedNext = false, availableDoorNumbers = emptyList(), selectedDoorNumber = null, availableSeatNumbers = emptyList(), selectedSeatNumber = null, confirmedBodyId = null) }
        if (type != null) filterDoorNumbers()
        updateButtonStates()
    }

    fun selectDoorNumber(doors: Int?) {
        _uiState.update { it.copy(selectedDoorNumber = doors, hasAttemptedNext = false, availableSeatNumbers = emptyList(), selectedSeatNumber = null, confirmedBodyId = null) }
        if (doors != null) filterSeatNumbers()
        updateButtonStates()
    }

    fun selectSeatNumber(seats: Int?) {
        _uiState.update { it.copy(selectedSeatNumber = seats, confirmedBodyId = null, hasAttemptedNext = false) }
        determineConfirmedBodyId()
        updateButtonStates()
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun resetSaveStatus() { _uiState.update { it.copy(isSaveSuccess = false, error = null) } }

    // --- Navigation ---

    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoadingVinDetails || currentState.isSaving || currentState.isLoadingNextStep) return

        _uiState.update { it.copy(hasAttemptedNext = true) }
        if (!isCurrentStepValid(currentState)) { updateButtonStates(); return }

        if (currentState.currentStep == AddVehicleStep.VIN) {
            decodeVinAndDecideNextStep()
            return
        }

        val nextStepOrdinal = currentState.currentStep.ordinal + 1
        if (nextStepOrdinal < AddVehicleStep.entries.size) {
            val nextStep = AddVehicleStep.entries[nextStepOrdinal]
            _uiState.update { it.copy(currentStep = nextStep, isLoadingNextStep = true, hasAttemptedNext = false) }
            viewModelScope.launch {
                prepareDataForStep(nextStep)
                _uiState.update { it.copy(isLoadingNextStep = false) }
                updateButtonStates()
            }
        }
    }

    fun goToPreviousStep() {
        val currentState = _uiState.value
        if (currentState.currentStep == AddVehicleStep.VIN || currentState.isLoadingVinDetails || currentState.isSaving || currentState.isLoadingNextStep) return

        val previousStepOrdinal = currentState.currentStep.ordinal - 1
        if (previousStepOrdinal >= 0) {
            val previousStep = AddVehicleStep.entries[previousStepOrdinal]
            _uiState.update { it.copy(currentStep = previousStep, error = null, hasAttemptedNext = false) }
            prepareDataForStep(previousStep)
            updateButtonStates()
        }
    }

    private fun decodeVinAndDecideNextStep() {
        _uiState.update { it.copy(isLoadingVinDetails = true, error = null) }
        viewModelScope.launch {
            vinDecoderRepository.decodeVin(_uiState.value.vinInput).onSuccess { decodedInfo ->
                if (decodedInfo.isEmpty()) {
                    _uiState.update { it.copy(isLoadingVinDetails = false, vinValidationError = "VIN not found in our database.") }
                } else {
                    _uiState.update { it.copy(isLoadingVinDetails = false, currentStep = AddVehicleStep.SERIES_YEAR, allDecodedOptions = decodedInfo, vinValidationError = null, hasAttemptedNext = false) }
                    prepareDataForStep(AddVehicleStep.SERIES_YEAR)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingVinDetails = false, vinValidationError = e.message ?: "Invalid VIN.") }
            }
            updateButtonStates()
        }
    }

    fun saveVehicle() {
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isSaving) return

        viewModelScope.launch {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            val clientId = jwtDecoder.getClientIdFromToken(token)
            val state = _uiState.value

            if (clientId == null) { _uiState.update { it.copy(error = "User session invalid.") }; return@launch }
            if (state.determinedModelId == null) { _uiState.update { it.copy(error = "Vehicle model not fully specified.") }; return@launch }
            if (state.mileageInput.isBlank()) { _uiState.update { it.copy(mileageValidationError = "Mileage is required.") }; return@launch }

            val saveRequest = VehicleSaveRequestDto(
                clientId = clientId,
                modelId = state.determinedModelId,
                vin = state.vinInput,
                mileage = state.mileageInput.toDouble()
            )

            _uiState.update { it.copy(isSaving = true, error = null) }
            vehicleRepository.saveVehicle(saveRequest).onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save vehicle.") }
            }
        }
    }

    // --- Private Helper & Filtering Functions ---

    private fun prepareDataForStep(step: AddVehicleStep) {
        when (step) {
            AddVehicleStep.SERIES_YEAR -> filterSeriesAndYears()
            AddVehicleStep.ENGINE_DETAILS -> filterEngineSizes()
            AddVehicleStep.BODY_DETAILS -> filterBodyTypes()
            AddVehicleStep.VEHICLE_INFO -> determineModelId()
            else -> {}
        }
    }

    private fun isCurrentStepValid(state: AddVehicleUiState): Boolean = when (state.currentStep) {
        AddVehicleStep.VIN -> state.vinInput.length == 17
        AddVehicleStep.SERIES_YEAR -> state.selectedSeriesName != null && state.selectedYear != null
        AddVehicleStep.ENGINE_DETAILS -> state.confirmedEngineId != null
        AddVehicleStep.BODY_DETAILS -> state.confirmedBodyId != null
        AddVehicleStep.VEHICLE_INFO -> state.mileageInput.isNotBlank() && state.determinedModelId != null
        AddVehicleStep.CONFIRM -> true
    }

    private fun updateButtonStates() {
        _uiState.update { state ->
            val commonLoading = state.isLoadingVinDetails || state.isSaving || state.isLoadingNextStep
            state.copy(
                isPreviousEnabled = state.currentStep.ordinal > AddVehicleStep.VIN.ordinal && !commonLoading,
                isNextEnabled = isCurrentStepValid(state) && !commonLoading
            )
        }
    }

    private fun getFilteredModels(): List<ModelDecodedDto> {
        val state = _uiState.value
        if (state.selectedSeriesName == null || state.selectedYear == null) return emptyList()
        return state.allDecodedOptions
            .filter { it.seriesName == state.selectedSeriesName }
            .flatMap { it.vehicleModelInfo }
            .filter { it.year == state.selectedYear }
    }

    private fun filterSeriesAndYears() {
        val uniqueSeriesYears = _uiState.value.allDecodedOptions
            .flatMap { dto -> dto.vehicleModelInfo.map { model -> dto.seriesName to model.year } }
            .distinct().sortedWith(compareBy({ it.first }, { it.second }))
        _uiState.update { it.copy(availableSeriesAndYears = uniqueSeriesYears) }
    }

    private fun filterEngineSizes() { _uiState.update { it.copy(availableEngineSizes = getFilteredModels().flatMap { m -> m.engineInfo }.map { e -> e.size }.distinct().sorted()) } }
    private fun filterEngineTypes() { _uiState.update { it.copy(availableEngineTypes = getFilteredModels().flatMap { m -> m.engineInfo }.filter { e -> e.size == it.selectedEngineSize }.map { e -> e.engineType }.distinct().sorted()) } }
    private fun filterTransmissions() { _uiState.update { it.copy(availableTransmissions = getFilteredModels().flatMap { m -> m.engineInfo }.filter { e -> e.size == it.selectedEngineSize && e.engineType == it.selectedEngineType }.map { e -> e.transmission }.distinct().sorted()) } }
    private fun filterDriveTypes() { _uiState.update { it.copy(availableDriveTypes = getFilteredModels().flatMap { m -> m.engineInfo }.filter { e -> e.size == it.selectedEngineSize && e.engineType == it.selectedEngineType && e.transmission == it.selectedTransmission }.map { e -> e.driveType }.distinct().sorted()) } }

    private fun determineConfirmedEngineId() {
        val state = _uiState.value
        val engine = getFilteredModels().flatMap { it.engineInfo }.find { e -> e.size == state.selectedEngineSize && e.engineType == state.selectedEngineType && e.transmission == state.selectedTransmission && e.driveType == state.selectedDriveType }
        _uiState.update { it.copy(confirmedEngineId = engine?.engineId) }
    }

    private fun filterBodyTypes() {
        val models = getFilteredModels().filter { m -> m.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableBodyTypes = models.flatMap { it.bodyInfo }.map { it.bodyType }.distinct().sorted()) }
    }

    private fun filterDoorNumbers() {
        val models = getFilteredModels().filter { m -> m.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableDoorNumbers = models.flatMap { it.bodyInfo }.filter { b -> b.bodyType == it.selectedBodyType }.map { b -> b.doorNumber }.distinct().sorted()) }
    }

    private fun filterSeatNumbers() {
        val models = getFilteredModels().filter { m -> m.engineInfo.any { e -> e.engineId == _uiState.value.confirmedEngineId } }
        _uiState.update { it.copy(availableSeatNumbers = models.flatMap { it.bodyInfo }.filter { b -> b.bodyType == it.selectedBodyType && b.doorNumber == it.selectedDoorNumber }.map { b -> b.seatNumber }.distinct().sorted()) }
    }

    private fun determineConfirmedBodyId() {
        val state = _uiState.value
        val models = getFilteredModels().filter { m -> m.engineInfo.any { e -> e.engineId == state.confirmedEngineId } }
        val body = models.flatMap { it.bodyInfo }.find { b -> b.bodyType == state.selectedBodyType && b.doorNumber == state.selectedDoorNumber && b.seatNumber == state.selectedSeatNumber }
        _uiState.update { it.copy(confirmedBodyId = body?.bodyId) }
    }

    private fun determineModelId() {
        val state = _uiState.value
        if (state.confirmedEngineId == null || state.confirmedBodyId == null) {
            _uiState.update { it.copy(determinedModelId = null) }
            return
        }
        val finalModel = getFilteredModels().find { model -> model.engineInfo.any { it.engineId == state.confirmedEngineId } && model.bodyInfo.any { it.bodyId == state.confirmedBodyId } }
        _uiState.update { it.copy(determinedModelId = finalModel?.modelId) }
    }
}