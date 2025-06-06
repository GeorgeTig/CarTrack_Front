package com.example.cartrack.feature.editreminder.presentation

import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto

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

    val isSaving: Boolean = false // Specific pentru acțiunea de salvare
)

sealed class EditReminderEvent {
    data class ShowMessage(val message: String) : EditReminderEvent()
    object NavigateBack : EditReminderEvent()
}