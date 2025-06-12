package com.example.cartrack.features.add_custom_reminder

import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto

data class AddCustomReminderState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Date pentru formular
    val availableTypes: List<ReminderTypeResponseDto> = emptyList(),
    val name: String = "",
    val selectedTypeId: Int? = null,
    val mileageInterval: String = "",
    val dateInterval: String = "",

    // Erori de validare
    val nameError: String? = null,
    val typeError: String? = null,
    val intervalError: String? = null
)

sealed class AddCustomReminderEvent {
    data class ShowMessage(val message: String) : AddCustomReminderEvent()
    object NavigateBack : AddCustomReminderEvent()
}