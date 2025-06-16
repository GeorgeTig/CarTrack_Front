package com.example.cartrack.features.add_custom_reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.CustomReminderRequestDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCustomReminderViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCustomReminderState())
    val uiState: StateFlow<AddCustomReminderState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddCustomReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            vehicleRepository.getAllReminderTypes().onSuccess { types ->
                _uiState.update { it.copy(isLoading = false, availableTypes = types) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name, nameError = null) } }
    fun onTypeSelected(typeId: Int?) { _uiState.update { it.copy(selectedTypeId = typeId, typeError = null) } }
    fun onMileageChange(mileage: String) { _uiState.update { it.copy(mileageInterval = mileage.filter { it.isDigit() }, intervalError = null) } }
    fun onDateChange(date: String) { _uiState.update { it.copy(dateInterval = date.filter { it.isDigit() }, intervalError = null) } }

    private fun validate(): Boolean {
        _uiState.update { it.copy(nameError = null, typeError = null, intervalError = null) }
        val state = _uiState.value
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Reminder name is required.") }; isValid = false
        }
        if (state.selectedTypeId == null) {
            _uiState.update { it.copy(typeError = "You must select a reminder type.") }; isValid = false
        }
        if (state.mileageInterval.isBlank() && state.dateInterval.isBlank()) {
            _uiState.update { it.copy(intervalError = "Please specify at least one interval (mileage or days).") }; isValid = false
        }
        return isValid
    }

    fun saveCustomReminder() {
        if (!validate()) return

        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            if (vehicleId == null) {
                _eventFlow.emit(AddCustomReminderEvent.ShowMessage("Error: No vehicle selected."))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }

            val request = CustomReminderRequestDto(
                name = _uiState.value.name.trim(),
                maintenanceTypeId = _uiState.value.selectedTypeId!!,
                mileageInterval = _uiState.value.mileageInterval.toIntOrNull() ?: -1,
                dateInterval = _uiState.value.dateInterval.toIntOrNull() ?: -1
            )

            vehicleRepository.addCustomReminder(vehicleId, request).onSuccess {
                _eventFlow.emit(AddCustomReminderEvent.ShowMessage("Custom reminder added successfully!"))
                _eventFlow.emit(AddCustomReminderEvent.NavigateBack)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}