package com.example.cartrack.feature.reminders.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto

// Enum-ul pentru dialogul de confirmare
sealed class ConfirmationDialogType(
    val title: String,
    val text: String,
    val icon: ImageVector
) {
    object DeactivateReminder : ConfirmationDialogType("Deactivate Reminder?", "You will no longer receive notifications for this reminder.", Icons.Default.NotificationsOff)
    object RestoreToDefault : ConfirmationDialogType("Restore Defaults?", "Your custom intervals will be lost. This action cannot be undone.", Icons.Default.Restore)
}

// Starea UI a fost simplificată
data class ReminderDetailState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val confirmationDialogType: ConfirmationDialogType? = null
)

sealed class ReminderDetailEvent {
    data class ShowMessage(val message: String) : ReminderDetailEvent()
    object NavigateBack : ReminderDetailEvent()
}