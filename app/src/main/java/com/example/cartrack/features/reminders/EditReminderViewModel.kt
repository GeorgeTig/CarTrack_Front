package com.example.cartrack.features.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditReminderState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadInitialData(reminderId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            vehicleRepository.getReminderById(reminderId).onSuccess { reminder ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reminder = reminder,
                        mileageIntervalInput = reminder.mileageInterval?.toString() ?: "",
                        timeIntervalInput = reminder.timeInterval.toString()
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMileageIntervalChanged(value: String) {
        _uiState.update { it.copy(mileageIntervalInput = value.filter { c -> c.isDigit() }, mileageIntervalError = null) }
    }

    fun onTimeIntervalChanged(value: String) {
        _uiState.update { it.copy(timeIntervalInput = value.filter { c -> c.isDigit() }, timeIntervalError = null) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val time = state.timeIntervalInput.toIntOrNull()
        if (time == null || time <= 0) {
            _uiState.update { it.copy(timeIntervalError = "Time interval is required and must be > 0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val request = ReminderUpdateRequestDto(
                id = state.reminder!!.configId,
                timeInterval = time,
                mileageInterval = state.mileageIntervalInput.toIntOrNull() ?: 0
            )
            vehicleRepository.updateReminder(request).onSuccess {
                _eventFlow.emit(EditReminderEvent.ShowMessage("Reminder updated!"))
                _eventFlow.emit(EditReminderEvent.NavigateBack)
            }.onFailure { e ->
                _eventFlow.emit(EditReminderEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}