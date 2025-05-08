package com.example.cartrack.core.ui.cards.ReminderCard

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ReminderStatusIcon(val icon: ImageVector, val tintColor: @Composable () -> Color) {

    // Status ID: 1 = Up to date
    object UpToDate : ReminderStatusIcon(
        icon = Icons.Filled.CheckCircle,
        tintColor = { Color(0xFF4CAF50) }
    )

    // Status ID: 2 = Due soon
    object DueSoon : ReminderStatusIcon(
        icon = Icons.Filled.Warning,
        tintColor = { MaterialTheme.colorScheme.tertiary }
    )

    // Status ID: 3 = Overdue
    object Overdue : ReminderStatusIcon(
        icon = Icons.Filled.Error,
        tintColor = { MaterialTheme.colorScheme.error }
    )

    companion object {
        fun fromStatusId(statusId: Int?): ReminderStatusIcon? {
            Log.d("ReminderStatusIcon", "Getting icon for status ID: $statusId")
            return when (statusId) {
                1 -> UpToDate
                2 -> DueSoon
                3 -> Overdue
                else -> {
                    Log.d("ReminderStatusIcon", "No specific icon mapped for status ID: $statusId")
                    null
                }
            }
        }
    }
}