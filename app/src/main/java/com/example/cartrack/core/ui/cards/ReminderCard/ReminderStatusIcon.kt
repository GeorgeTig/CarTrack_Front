package com.example.cartrack.core.ui.cards.ReminderCard

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable


sealed class ReminderStatusIcon(val icon: ImageVector, val tintColor: @Composable () -> Color) {
    object Completed : ReminderStatusIcon( // Mapped to ID 1
        icon = Icons.Filled.CheckCircle,
        tintColor = { Color(0xFF2E7D32) } // Dark Green
    )
    object DueSoon : ReminderStatusIcon( // Mapped to ID 2
        icon = Icons.Filled.Warning,
        tintColor = { MaterialTheme.colorScheme.tertiary }
    )
    object Overdue : ReminderStatusIcon( // Mapped to ID 3
        icon = Icons.Filled.Clear,
        tintColor = { MaterialTheme.colorScheme.error }
    )
    // Add other IDs if necessary

    companion object {
        // --- Renamed function and changed parameter type ---
        fun fromStatusId(statusId: Int?): ReminderStatusIcon? {
            Log.d("StatusIconDebug", "Checking status ID: $statusId")

            return when (statusId) {
                1 -> {
                    Log.d("StatusIconDebug", "Matched ID 1: COMPLETED")
                    Completed
                }
                2 -> {
                    Log.d("StatusIconDebug", "Matched ID 2: DUESOON")
                    DueSoon
                }
                3 -> {
                    Log.d("StatusIconDebug", "Matched ID 3: OVERDUE")
                    Overdue
                }
                // Add cases for other IDs if they exist
                else -> {
                    // Includes null or any other ID that doesn't have a specific icon (like 'Active')
                    Log.d("StatusIconDebug", "No icon match found for ID: $statusId")
                    null
                }
            }
        }
    }
}