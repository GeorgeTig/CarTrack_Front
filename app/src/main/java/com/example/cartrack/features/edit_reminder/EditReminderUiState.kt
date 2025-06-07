package com.example.cartrack.features.edit_reminder

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto

data class EditReminderState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,

    // Câmpurile formularului
    val mileageIntervalInput: String = "",
    val timeIntervalInput: String = "",

    // Erorile formularului
    val mileageIntervalError: String? = null,
    val timeIntervalError: String? = null,

    val isSaving: Boolean = false
)

sealed class EditReminderEvent {
    data class ShowMessage(val message: String) : EditReminderEvent()
    object NavigateBack : EditReminderEvent()
}