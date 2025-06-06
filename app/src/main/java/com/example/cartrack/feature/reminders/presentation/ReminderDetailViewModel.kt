package com.example.cartrack.feature.reminders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
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
class ReminderDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderDetailState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // --- ÎNCĂRCARE DATE ---

    fun loadReminderDetails(reminderId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // APELUL ESTE ACUM DIRECT ȘI EFICIENT
            val result = vehicleRepository.getReminderById(reminderId)
            result.onSuccess { reminder ->
                _uiState.update { it.copy(isLoading = false, reminder = reminder) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load details.") }
            }
        }
    }

    // --- GESTIONARE DIALOG CONFIRMARE ---

    fun showConfirmationDialog(type: ConfirmationDialogType) {
        _uiState.update { it.copy(confirmationDialogType = type) }
    }

    fun dismissConfirmationDialog() {
        _uiState.update { it.copy(confirmationDialogType = null) }
    }

    fun onConfirmAction() {
        val type = _uiState.value.confirmationDialogType ?: return

        when (type) {
            is ConfirmationDialogType.DeactivateReminder -> executeToggleActiveStatus()
            is ConfirmationDialogType.RestoreToDefault -> executeRestoreToDefaults()
        }

        dismissConfirmationDialog()
    }

    // --- ACȚIUNI EFECTIVE ---

    fun executeToggleActiveStatus() {
        val reminder = _uiState.value.reminder ?: return
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderActiveStatus(reminder.configId)
            result.onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Status updated!"))
                loadReminderDetails(reminder.configId) // Reîncarcă detaliile
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    fun executeRestoreToDefaults() {
        val reminder = _uiState.value.reminder ?: return
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderToDefault(reminder.configId)
            result.onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Restored to defaults!"))
                loadReminderDetails(reminder.configId) // Reîncarcă pentru a afișa noile valori
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

}