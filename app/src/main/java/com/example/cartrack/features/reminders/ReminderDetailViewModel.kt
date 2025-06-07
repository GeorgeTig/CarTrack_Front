package com.example.cartrack.features.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ReminderDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderDetailState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadReminderDetails(reminderId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            vehicleRepository.getReminderById(reminderId).onSuccess { reminder ->
                _uiState.update { it.copy(isLoading = false, reminder = reminder) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun showConfirmationDialog(type: ConfirmationDialogType) {
        _uiState.update { it.copy(confirmationDialogType = type) }
    }

    fun dismissConfirmationDialog() {
        _uiState.update { it.copy(confirmationDialogType = null) }
    }

    fun onConfirmAction() {
        when (_uiState.value.confirmationDialogType) {
            is ConfirmationDialogType.DeactivateReminder -> executeToggleActiveStatus()
            is ConfirmationDialogType.RestoreToDefault -> executeRestoreToDefaults()
            null -> return
        }
        dismissConfirmationDialog()
    }

    fun executeToggleActiveStatus() {
        val reminderId = _uiState.value.reminder?.configId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            vehicleRepository.updateReminderActiveStatus(reminderId).onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Status updated!"))
                loadReminderDetails(reminderId) // Refresh data
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    private fun executeRestoreToDefaults() {
        val reminderId = _uiState.value.reminder?.configId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            vehicleRepository.updateReminderToDefault(reminderId).onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Reminder restored to defaults!"))
                loadReminderDetails(reminderId)
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }
}