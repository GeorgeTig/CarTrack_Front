package com.example.cartrack.features.reminders.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getStatusText(statusId: Int): String = when (statusId) {
    1 -> "Up to date"
    2 -> "Due soon"
    3 -> "Overdue"
    else -> "Unknown"
}

@Composable
fun getTintColorForStatus(statusId: Int): Color = when (statusId) {
    1 -> Color(0xFF4CAF50)
    2 -> MaterialTheme.colorScheme.tertiary
    3 -> MaterialTheme.colorScheme.error
    else -> LocalContentColor.current
}

fun getIconForStatus(statusId: Int): ImageVector = when (statusId) {
    1 -> Icons.Filled.CheckCircle
    2 -> Icons.Filled.Warning
    3 -> Icons.Filled.Error
    else -> Icons.Filled.HelpOutline
}