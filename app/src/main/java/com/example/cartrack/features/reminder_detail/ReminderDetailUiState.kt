package com.example.cartrack.features.reminder_detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto

sealed class ConfirmationDialogType(
    val title: String, val text: String, val icon: ImageVector
) {
    object DeactivateReminder : ConfirmationDialogType("Deactivate Reminder?", "You will no longer receive notifications for this reminder.", Icons.Default.NotificationsOff)
    object ResetToDefault : ConfirmationDialogType("Restore Defaults?", "Your custom intervals will be lost. This cannot be undone.", Icons.Default.Restore)
    // --- TIP NOU DE DIALOG ---
    object DeleteCustomReminder : ConfirmationDialogType("Delete Reminder?", "This custom reminder will be permanently deleted.", Icons.Default.DeleteForever)
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
    // --- EVENIMENT NOU ---
    object NavigateBack : ReminderDetailEvent()
}