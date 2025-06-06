package com.example.cartrack.feature.editreminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.vehicle.data.model.ReminderRequestDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditReminderState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadInitialData(reminderId: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            if (vehicleId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Vehicle not found.") }
                return@launch
            }

            val result = vehicleRepository.getRemindersByVehicleId(vehicleId)
            result.onSuccess { reminders ->
                val reminder = reminders.find { it.configId == reminderId }
                if (reminder != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reminder = reminder,
                            mileageIntervalInput = reminder.mileageInterval?.toString() ?: "",
                            timeIntervalInput = reminder.timeInterval.toString()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Reminder not found.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMileageIntervalChanged(value: String) {
        val digits = value.filter { it.isDigit() }
        _uiState.update { it.copy(mileageIntervalInput = digits, mileageIntervalError = null) }
    }

    fun onTimeIntervalChanged(value: String) {
        val digits = value.filter { it.isDigit() }
        _uiState.update { it.copy(timeIntervalInput = digits, timeIntervalError = null) }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        val reminderId = currentState.reminder?.configId ?: return

        // Validare
        val timeInterval = currentState.timeIntervalInput.toIntOrNull()
        if (timeInterval == null || timeInterval <= 0) {
            _uiState.update { it.copy(timeIntervalError = "Time interval is required and must be > 0") }
            return
        }
        val mileageInterval = currentState.mileageIntervalInput.toIntOrNull() ?: 0

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val request = ReminderRequestDto(
                id = reminderId,
                mileageInterval = mileageInterval,
                timeInterval = timeInterval
            )
            val result = vehicleRepository.updateReminder(request)
            result.onSuccess {
                _eventFlow.emit(EditReminderEvent.ShowMessage("Reminder updated successfully!"))
                _eventFlow.emit(EditReminderEvent.NavigateBack)
            }.onFailure { e ->
                _eventFlow.emit(EditReminderEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}