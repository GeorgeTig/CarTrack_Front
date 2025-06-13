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
        // Nu mai apelăm aici, pentru a permite controlul din UI la intrare și la refresh
    }

    // Funcția este acum publică pentru a putea fi apelată din UI
    fun loadReminderDetails() {
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
        _uiState.update { it.copy(confirmationDialogType = null, isActionLoading = false) }
    }

    fun onConfirmAction() {
        when (_uiState.value.confirmationDialogType) {
            is ConfirmationDialogType.DeactivateReminder -> executeToggleActiveStatus()
            is ConfirmationDialogType.ResetToDefault -> executeResetToDefaults()
            is ConfirmationDialogType.DeleteCustomReminder -> executeDeleteCustomReminder()
            null -> return
        }
    }

    fun executeToggleActiveStatus() {
        dismissConfirmationDialog()
        performAction(
            action = { vehicleRepository.updateReminderActiveStatus(reminderId) },
            successMessage = "Status updated successfully!"
        )
    }

    private fun executeResetToDefaults() {
        dismissConfirmationDialog()
        performAction(
            action = { vehicleRepository.resetReminderToDefault(reminderId) },
            successMessage = "Reminder has been reset to default values."
        )
    }

    private fun executeDeleteCustomReminder() {
        dismissConfirmationDialog()
        performAction(
            action = { vehicleRepository.deactivateCustomReminder(reminderId) },
            successMessage = "Reminder has been deleted.",
            isDeleteAction = true
        )
    }

    private fun performAction(
        action: suspend () -> Result<Unit>,
        successMessage: String,
        isDeleteAction: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            action().onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage(successMessage))
                if (isDeleteAction) {
                    _eventFlow.emit(ReminderDetailEvent.NavigateBack)
                } else {
                    loadReminderDetails() // Refresh data
                }
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }
}