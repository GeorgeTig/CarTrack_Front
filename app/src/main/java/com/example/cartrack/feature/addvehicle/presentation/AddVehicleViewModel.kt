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

    // ... (restul funcțiilor de selectare, filtrare, determinare ID-uri rămân la fel ca în versiunea anterioară completă)
    fun onVinInputChange(newVin: String) {
        if (_uiState.value.currentStep != AddVehicleStep.VIN) return
        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase().take(17)
        _uiState.update {
            it.copy(
                vinInput = processedVin,
                vinValidationError = if (processedVin.isNotEmpty() && processedVin.length != 17) "VIN must be 17 characters" else null,
                error = null
            )
        }
        updateButtonStates()
    }

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

    // NOU: Funcție dedicată pentru când utilizatorul apasă "Skip VIN / Enter Manually"
    fun userClickedSkipVinOrEnterManually() {
        Log.d(logTag, "User chose to skip VIN or enter manually. Moving to SERIES_YEAR.")
        _uiState.update {
            it.copy(
                currentStep = AddVehicleStep.SERIES_YEAR, // Mergi la pasul următor pentru input manual
                allDecodedOptions = emptyList(),      // Golește opțiunile din VIN
                vinInput = it.vinInput, // Păstrează VIN-ul dacă a fost tastat, dar nu va fi folosit pentru populare
                vinValidationError = null,        // Resetează eroarea VIN
                isLoadingVinDetails = false,    // Oprește orice loading de VIN
                // Resetează toate selecțiile dependente
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
        prepareDataForStep(AddVehicleStep.SERIES_YEAR) // Va popula `availableSeriesAndYears` ca goală
        updateButtonStates()
    }


    fun goToNextStep() {
        val currentState = _uiState.value
        if (!currentState.isNextEnabled || currentState.isLoadingVinDetails || currentState.isSaving || currentState.isLoadingNextStep) return
        _uiState.update { it.copy(error = null) }

        if (currentState.currentStep == AddVehicleStep.VIN) {
            // La pasul VIN, "Next" înseamnă întotdeauna "încearcă decodare"
            decodeVinAndDecideNextStep()
            return // decodeVinAndDecideNextStep se ocupă de actualizarea stării și a pasului
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

    fun goToPreviousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep == AddVehicleStep.VIN || _uiState.value.isLoadingVinDetails || _uiState.value.isSaving || _uiState.value.isLoadingNextStep) return

        val previousStepOrdinal = currentStep.ordinal - 1
        if (previousStepOrdinal < 0) return

        val previousStep = AddVehicleStep.values()[previousStepOrdinal]
        Log.d(logTag, "Moving back from $currentStep to $previousStep")

        // Asigură-te că resetezi erorile când mergi înapoi
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



    private fun decodeVinAndDecideNextStep() { /* ... implementare existentă ... */
        val vin = _uiState.value.vinInput
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be 17 characters.") }
            updateButtonStates() // Update buttons to reflect validation error
            return
        }
        _uiState.update { it.copy(vinValidationError = null, isLoadingVinDetails = true, error = null, isNextEnabled = false, isPreviousEnabled = false) }

        viewModelScope.launch {
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoadingVinDetails = false, error = "Cannot identify user.") }
                updateButtonStates()
                return@launch
            }

            Log.d(logTag, "Decoding VIN: $vin for client ID: $clientId")
            val result = vinDecoderRepository.decodeVin(vin, clientId)
            // Chiar dacă eșuează, mergem la SERIES_YEAR pentru input manual
            var nextStepAfterVinProcessing = AddVehicleStep.SERIES_YEAR

            result.onSuccess { decodedInfoList ->
                Log.d(logTag, "VIN Decode Success: ${decodedInfoList.size} options found.")
                if (decodedInfoList.isEmpty()) {
                    _uiState.update { it.copy(error = "No vehicle info for this VIN. Please provide details manually.", allDecodedOptions = emptyList()) }
                } else {
                    _uiState.update {
                        it.copy(
                            allDecodedOptions = decodedInfoList,
                            selectedSeriesName = null, selectedYear = null, // Resetează pentru noul set de opțiuni
                            // ... resetează și celelalte selecții granulare ...
                            selectedEngineSize = null, selectedEngineType = null, selectedTransmission = null, selectedDriveType = null, confirmedEngineId = null,
                            selectedBodyType = null, selectedDoorNumber = null, selectedSeatNumber = null, confirmedBodyId = null,
                            determinedModelId = null
                        )
                    }
                }
            }.onFailure { exception ->
                Log.e(logTag, "VIN Decode Failed", exception)
                _uiState.update { it.copy(error = exception.message ?: "Failed to decode VIN. Please provide details manually.", allDecodedOptions = emptyList()) }
            }
            _uiState.update { it.copy(isLoadingVinDetails = false, currentStep = nextStepAfterVinProcessing) }
            prepareDataForStep(nextStepAfterVinProcessing)
            updateButtonStates()
        }
    }

    private fun prepareDataForStep(step: AddVehicleStep) { /* ... implementare existentă ... */
        Log.d(logTag, "Preparing data for step: $step")
        when (step) {
            AddVehicleStep.SERIES_YEAR -> filterSeriesAndYears()
            AddVehicleStep.ENGINE_DETAILS -> filterEngineSizes()
            AddVehicleStep.BODY_DETAILS -> filterBodyTypes()
            AddVehicleStep.VEHICLE_INFO -> determineModelId()
            else -> { /* No specific data prep for VIN or CONFIRM here */ }
        }
    }

    // --- Funcțiile de filtrare (filterSeriesAndYears, filterEngineSizes, etc.) și determinare ID (determineConfirmedEngineId, etc.)
    // rămân la fel ca în versiunea anterioară completă a ViewModel-ului. Le voi omite aici pentru concizie,
    // dar asigură-te că sunt prezente și corecte.
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
        // Extrage producătorul din seriesNameFilter (care e "Producător Serie")
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


    private fun updateButtonStates() { /* ... implementare existentă ... */
        _uiState.update { state ->
            val commonLoading = state.isLoadingVinDetails || state.isSaving || state.isLoadingNextStep
            val isPrevEnabled = state.currentStep.ordinal > AddVehicleStep.VIN.ordinal && !commonLoading

            var nextStepPossible = true
            when (state.currentStep) {
                AddVehicleStep.VIN -> nextStepPossible = state.vinInput.length == 17 && state.vinValidationError == null
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

    fun saveVehicle() { /* ... implementare existentă, dar folosește jwtDecoder.getClientIdFromToken() ... */
        if (_uiState.value.currentStep != AddVehicleStep.CONFIRM || _uiState.value.isSaving) return

        val state = _uiState.value
        viewModelScope.launch {
            val clientId = jwtDecoder.getClientIdFromToken() // Obține clientId aici
            var validationError: String? = null

            when {
                clientId == null -> validationError = "User not identified."
                // Dacă allDecodedOptions e gol, înseamnă că suntem pe flux manual complet
                // Atunci VIN-ul nu mai e obligatoriu dacă celelalte câmpuri manuale sunt completate
                state.allDecodedOptions.isNotEmpty() && state.vinInput.length != 17 -> validationError = "Invalid VIN."
                state.allDecodedOptions.isEmpty() && state.vinInput.isNotBlank() && state.vinInput.length != 17 -> validationError = "VIN must be 17 chars if entered."

                state.selectedSeriesName == null -> validationError = "Vehicle series not selected/entered."
                state.selectedYear == null -> validationError = "Vehicle year not selected/entered."
                // Pentru fluxul manual, confirmedEngineId și confirmedBodyId pot fi null
                // Backend-ul va trebui să gestioneze asta sau vom trimite valori placeholder.
                // Momentan, dacă datele vin din VIN, acestea sunt necesare.
                state.allDecodedOptions.isNotEmpty() && state.confirmedEngineId == null -> validationError = "Engine details not confirmed."
                state.allDecodedOptions.isNotEmpty() && state.confirmedBodyId == null -> validationError = "Body details not confirmed."
                state.determinedModelId == null && state.allDecodedOptions.isNotEmpty() -> validationError = "Could not determine specific vehicle model."
                // Pentru flux manual, determinedModelId va fi probabil null/placeholder
                state.mileageInput.isBlank() || state.mileageInput.toDoubleOrNull() == null || state.mileageInput.toDouble() < 0 -> validationError = "Valid mileage is required."
                else -> {}
            }

            if (validationError != null) {
                _uiState.update { it.copy(error = "Cannot save: $validationError", isPreviousEnabled = true, isSaving = false) }
                Log.e(logTag, "Save validation failed: $validationError")
                return@launch
            }

            // Pentru flux manual, modelId ar putea fi un ID special sau backend-ul creează un model nou.
            // Aici presupunem că determinedModelId are o valoare validă dacă am ajuns aici fără eroare.
            val finalModelIdToSave = state.determinedModelId ?: run {
                // Dacă suntem pe flux manual și determinedModelId e null, trebuie o strategie.
                // Momentan, vom considera eroare dacă nu e determinat și am avut opțiuni VIN.
                if (state.allDecodedOptions.isNotEmpty()) {
                    _uiState.update { it.copy(error = "Model ID missing.", isSaving = false, isPreviousEnabled = true) }
                    Log.e(logTag, "Model ID is null before save, but VIN options were present.")
                    return@launch
                }
                // TODO: Definește un ModelID placeholder pentru fluxul complet manual dacă backend-ul o cere
                // sau ajustează backend-ul să creeze un model nou pe baza Serie/An/etc.
                // Pentru acum, vom lăsa să crape dacă e null și am avut opțiuni.
                // Sau, dacă allDecodedOptions e gol, trimitem un ID special (ex: 0) și backend-ul știe.
                // Acest modelId va fi cel mai probabil 0 dacă nu există date din VIN
                0 // Placeholder pentru "creează model nou" sau "model necunoscut"
            }


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