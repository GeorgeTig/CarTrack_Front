package com.example.cartrack.features.reminders.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.ui.getIconForMaintenanceType

// --- ReminderItemCard (pentru liste) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemCard(reminder: ReminderResponseDto, onClick: () -> Unit) {
    val typeIcon = getIconForMaintenanceType(reminder.typeId)
    val statusIcon = if (reminder.isActive) getIconForStatus(reminder.statusId) else Icons.Outlined.PauseCircle
    val statusColor = if (reminder.isActive) getTintColorForStatus(reminder.statusId) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = typeIcon, contentDescription = reminder.typeName, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = reminder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(4.dp))
                    Icon(imageVector = statusIcon, contentDescription = "Status", tint = statusColor, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, "Due Date", Modifier.size(14.dp).padding(end=4.dp), tint = statusColor)
                    Text(text = "Due: ${reminder.dueDate.take(10)}", style = MaterialTheme.typography.bodySmall, color = statusColor)
                }
            }
            if (reminder.isEditable) {
                Icon(Icons.Filled.EditNote, "Editable", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// --- ReminderDetailCard (pentru ecranul de detalii) ---
@Composable
fun ReminderDetailCard(reminder: ReminderResponseDto) {
    val typeIcon = getIconForMaintenanceType(reminder.typeId)
    val statusIcon = getIconForStatus(reminder.statusId)
    val statusColor = getTintColorForStatus(reminder.statusId)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = typeIcon, contentDescription = "Type", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Text(text = reminder.name, style = MaterialTheme.typography.titleLarge)
            }
            Divider(Modifier.padding(vertical = 12.dp))

            // Details Section
            MiniCardSection(title = "Details", icon = Icons.AutoMirrored.Filled.ListAlt) {
                DetailRow("Type:", reminder.typeName)
                DetailRow("Status:", getStatusText(reminder.statusId), statusColor)
                DetailRow("Reminder Active:", if (reminder.isActive) "Yes" else "No")
            }
            Spacer(Modifier.height(16.dp))

            // Configuration Section
            MiniCardSection(title = "Configuration", icon = Icons.Filled.Tune) {
                DetailRow("Time Interval:", "${reminder.timeInterval} days")
                DetailRow("Mileage Interval:", reminder.mileageInterval?.let { "$it mi" } ?: "Not set")
            }
        }
    }
}

// --- Helper Components ---
@Composable
private fun MiniCardSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Divider()
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(
            text = "$label ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
