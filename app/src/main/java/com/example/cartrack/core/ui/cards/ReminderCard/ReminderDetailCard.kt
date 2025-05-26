package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailCard(
    modifier: Modifier = Modifier,
    reminder: ReminderResponseDto
) {
    val typeIconInfo = MaintenanceTypeIcon.fromTypeId(reminder.typeId)
    val statusIconInfo = ReminderStatusIcon.fromStatusId(reminder.statusId)

    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Main Header: Icon + Full Name + Status Icon ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = typeIconInfo.icon,
                contentDescription = reminder.typeName,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = reminder.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            statusIconInfo?.let {
                Icon(
                    imageVector = it.icon,
                    contentDescription = "Status",
                    tint = it.tintColor(),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Divider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp))


        // Section 1: Details
        MiniCardSection(title = "Details", icon = Icons.AutoMirrored.Filled.ListAlt) {
            DetailRow("Type:", reminder.typeName)
            // Get a friendly status name
            val friendlyStatus = when(reminder.statusId) {
                1 -> "Up to date"
                2 -> "Due soon"
                3 -> "Overdue"
                else -> "Unknown (${reminder.statusId})"
            }
            // Display status with its specific color
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 100.dp, max = 120.dp).padding(end = 8.dp)
                )
                Text(
                    text = friendlyStatus,
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                )
            }
            DetailRow("Reminder Active:", if (reminder.isActive) "Yes" else "No")
        }

        // Section 2: Configuration
        MiniCardSection(title = "Configuration", icon = Icons.Filled.Tune) {
            val timeIntervalText = "${reminder.timeInterval} day${if (reminder.timeInterval == 1) "" else "s"}"
            DetailRow("Time Interval:", timeIntervalText)
            reminder.mileageInterval?.let {
                DetailRow("Mileage Interval:", "$it mi")
            } ?: DetailRow("Mileage Interval:", "Not set")
        }

        // Section 3: Service Tracking
        MiniCardSection(title = "Service Tracking", icon = Icons.Filled.CalendarToday) {
            // Next Due - Color based on status
            Text("Next Due", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium, color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) { // Indent details slightly
                Icon(Icons.Filled.Event, contentDescription = "Due Date", modifier = Modifier.size(16.dp).padding(end = 6.dp), tint = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurfaceVariant)
                Text(reminder.dueDate.take(10), style = MaterialTheme.typography.bodyMedium, color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Filled.Speed, contentDescription = "Due Mileage", modifier = Modifier.size(16.dp).padding(end = 6.dp), tint = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${reminder.dueMileage.toInt()} mi", style = MaterialTheme.typography.bodyMedium, color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last Performed - Use standard text color
            Text("Last Performed", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 4.dp), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Filled.EventAvailable, contentDescription = "Last Date", modifier = Modifier.size(16.dp).padding(end = 6.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(reminder.lastDateCheck.take(10), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Filled.History, contentDescription = "Last Mileage", modifier = Modifier.size(16.dp).padding(end = 6.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) // Changed icon
                Text("${reminder.lastMileageCheck.toInt()} mi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

    }
}