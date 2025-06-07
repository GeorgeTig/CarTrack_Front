package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto

@Composable
fun ReminderDetailCard(reminder: ReminderResponseDto, modifier: Modifier = Modifier) {
    val typeIcon = MaintenanceTypeIcon.fromTypeId(reminder.typeId)
    val statusInfo = ReminderStatusIcon.from(reminder.isActive, reminder.statusId)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Antet
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(typeIcon.icon, reminder.typeName, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(reminder.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            statusInfo?.let {
                Icon(it.icon, "Status", tint = it.color(), modifier = Modifier.size(24.dp))
            }
        }
        Divider()

        // Secțiuni de detalii
        MiniCardSection(title = "Details", icon = Icons.AutoMirrored.Filled.ListAlt) {
            DetailRow("Type:", reminder.typeName)
            DetailRow("Status:", getFriendlyStatus(reminder, statusInfo))
            DetailRow("Reminder Active:", if (reminder.isActive) "Yes" else "No")
        }
        MiniCardSection(title = "Configuration", icon = Icons.Filled.Tune) {
            DetailRow("Time Interval:", "${reminder.timeInterval} days")
            DetailRow("Mileage Interval:", reminder.mileageInterval?.let { "$it mi" } ?: "Not set")
        }
        MiniCardSection(title = "Service Tracking", icon = Icons.Filled.CalendarToday) {
            // ... (logica pentru afișarea datelor și a kilometrajului due/last)
        }
    }
}

// Helper composables pentru Card
@Composable
private fun MiniCardSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                Icon(icon, title, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Divider()
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.width(120.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun getFriendlyStatus(reminder: ReminderResponseDto, statusInfo: ReminderStatusIcon?): String {
    if (!reminder.isActive) return "Inactive"
    return when(statusInfo) {
        is ReminderStatusIcon.UpToDate -> "Up to date"
        is ReminderStatusIcon.DueSoon -> "Due soon"
        is ReminderStatusIcon.Overdue -> "Overdue"
        else -> "Unknown"
    }
}