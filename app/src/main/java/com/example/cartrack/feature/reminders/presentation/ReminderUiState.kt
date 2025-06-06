package com.example.cartrack.feature.reminders.presentation

import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.feature.maintenance.presentation.EditReminderFormState

// State-ul paginii de detalii
data class ReminderDetailState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,
    val isEditDialogVisible: Boolean = false,
    val editFormState: EditReminderFormState = EditReminderFormState(),
    val isActionLoading: Boolean = false // Specific pentru acțiunile de pe butoane
)

// Evenimente de la ViewModel către UI
sealed class ReminderDetailEvent {
    data class ShowMessage(val message: String) : ReminderDetailEvent()
    object NavigateBack : ReminderDetailEvent()
}