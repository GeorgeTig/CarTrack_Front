package com.example.cartrack.features.reminders

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto

// --- Stări și evenimente pentru ReminderDetailScreen ---

sealed class ConfirmationDialogType(
    val title: String, val text: String, val icon: ImageVector
) {
    object DeactivateReminder : ConfirmationDialogType("Deactivate Reminder?", "You will no longer receive notifications for this reminder.", Icons.Default.NotificationsOff)
    object RestoreToDefault : ConfirmationDialogType("Restore Defaults?", "Your custom intervals will be lost. This cannot be undone.", Icons.Default.Restore)
}

data class ReminderDetailState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val confirmationDialogType: ConfirmationDialogType? = null
)

sealed class ReminderDetailEvent {
    data class ShowMessage(val message: String) : ReminderDetailEvent()
}

// --- Stări și evenimente pentru EditReminderScreen ---

data class EditReminderState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,
    val mileageIntervalInput: String = "",
    val timeIntervalInput: String = "",
    val mileageIntervalError: String? = null,
    val timeIntervalError: String? = null,
    val isSaving: Boolean = false
)

sealed class EditReminderEvent {
    data class ShowMessage(val message: String) : EditReminderEvent()
    object NavigateBack : EditReminderEvent()
}