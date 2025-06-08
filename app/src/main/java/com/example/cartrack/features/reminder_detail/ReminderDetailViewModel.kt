package com.example.cartrack.features.reminder_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ReminderDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderDetailState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val reminderId: Int = checkNotNull(savedStateHandle[Routes.REMINDER_ARG_ID])

    init {
        loadReminderDetails()
    }

    private fun loadReminderDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            vehicleRepository.getReminderById(reminderId).onSuccess { reminder ->
                _uiState.update { it.copy(isLoading = false, reminder = reminder) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun onActionSuccess(message: String) {
        viewModelScope.launch {
            _eventFlow.emit(ReminderDetailEvent.ActionSuccess(message))
            loadReminderDetails()
        }
    }

    fun showConfirmationDialog(type: ConfirmationDialogType) { _uiState.update { it.copy(dialogType = type) } }
    fun dismissConfirmationDialog() { _uiState.update { it.copy(dialogType = null) } }

    fun onConfirmAction() {
        when (_uiState.value.dialogType) {
            is ConfirmationDialogType.DeactivateReminder -> executeToggleActiveStatus()
            is ConfirmationDialogType.RestoreToDefault -> executeRestoreToDefaults()
            null -> Unit
        }
        dismissConfirmationDialog()
    }

    fun executeToggleActiveStatus() {
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            vehicleRepository.updateReminderActiveStatus(reminderId).onSuccess {
                onActionSuccess("Status updated!")
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    private fun executeRestoreToDefaults() {
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            vehicleRepository.updateReminderToDefault(reminderId).onSuccess {
                onActionSuccess("Restored to defaults!")
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }
}