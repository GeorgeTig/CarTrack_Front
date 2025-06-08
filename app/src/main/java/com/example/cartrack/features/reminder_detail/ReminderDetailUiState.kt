package com.example.cartrack.features.reminder_detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto

// Enum pentru a defini tipurile de dialoguri de confirmare
sealed class ConfirmationDialogType(
    val title: String,
    val text: String,
    val icon: ImageVector
) {
    data object DeactivateReminder : ConfirmationDialogType(
        "Deactivate Reminder?",
        "You will no longer receive notifications for this reminder.",
        Icons.Default.NotificationsOff
    )
    data object RestoreToDefault : ConfirmationDialogType(
        "Restore Defaults?",
        "Your custom intervals will be lost. This action cannot be undone.",
        Icons.Default.Restore
    )
}

// Starea UI a ecranului
data class ReminderDetailState(
    val isLoading: Boolean = true,
    val reminder: ReminderResponseDto? = null,
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val dialogType: ConfirmationDialogType? = null
)

sealed class ReminderDetailEvent {
    data class ShowMessage(val message: String) : ReminderDetailEvent()
    data class ActionSuccess(val message: String) : ReminderDetailEvent()
}
