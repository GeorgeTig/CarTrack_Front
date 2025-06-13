package com.example.cartrack.features.edit_reminder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.ReminderUpdateRequestDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.navigation.Routes
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
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditReminderState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditReminderEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val reminderId: Int = checkNotNull(savedStateHandle[Routes.REMINDER_ARG_ID])

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            vehicleRepository.getReminderById(reminderId).onSuccess { reminder ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reminder = reminder,
                        mileageIntervalInput = if(reminder.mileageInterval > 0) reminder.mileageInterval.toString() else "",
                        timeIntervalInput = reminder.timeInterval.toString()
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMileageIntervalChanged(value: String) {
        _uiState.update { it.copy(mileageIntervalInput = value.filter { it.isDigit() }, mileageIntervalError = null) }
    }

    fun onTimeIntervalChanged(value: String) {
        _uiState.update { it.copy(timeIntervalInput = value.filter { it.isDigit() }, timeIntervalError = null) }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        val timeInterval = state.timeIntervalInput.toIntOrNull()
        if (timeInterval == null || timeInterval <= 0) {
            _uiState.update { it.copy(timeIntervalError = "Time interval is required and must be > 0") }
            return false
        }
        return true
    }

    fun saveChanges() {
        if (!validateInputs()) return

        val currentState = _uiState.value
        val timeInterval = currentState.timeIntervalInput.toInt()
        val mileageInterval = currentState.mileageIntervalInput.toIntOrNull() ?: 0

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val request = ReminderUpdateRequestDto(
                id = reminderId,
                mileageInterval = mileageInterval,
                timeInterval = timeInterval
            )
            vehicleRepository.updateReminder(request).onSuccess {
                // --- MODIFICARE AICI ---
                // Emitem ambele evenimente: mesaj și navigare
                _eventFlow.emit(EditReminderEvent.ShowMessage("Reminder updated successfully!"))
                _eventFlow.emit(EditReminderEvent.NavigateBackWithResult) // Eveniment nou
                // --- SFÂRȘIT MODIFICARE ---
            }.onFailure { e ->
                _eventFlow.emit(EditReminderEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}