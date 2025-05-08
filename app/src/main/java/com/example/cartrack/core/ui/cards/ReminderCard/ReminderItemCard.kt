package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemCard(
    modifier: Modifier = Modifier,
    reminder: ReminderResponseDto?
) {
    // --- Get icons based on NEW DTO fields ---
    val typeIconInfo = MaintenanceTypeIcon.fromTypeId(reminder?.typeId)
    val statusIconInfo = ReminderStatusIcon.fromStatusId(reminder?.statusId)

    LaunchedEffect(reminder?.configId) { }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Leading Content: Type Icon ---
            Icon(
                imageVector = typeIconInfo.icon,
                contentDescription = reminder?.typeName ?: "Maintenance Type",
                modifier = Modifier.size(40.dp).padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // --- Middle Content: Name, Status Icon, Due Info ---
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp)
                    )
                    // Status Icon
                    statusIconInfo?.let { statusInfo ->
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = "Status",
                            tint = statusInfo.tintColor(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Due Date Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, contentDescription = "Due Date", modifier = Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Due: ${reminder?.dueDate?.take(10) ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }

                // Due Mileage Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, contentDescription = "Due Mileage", modifier = Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "At: ${reminder?.dueMileage?.toInt() ?: "-"} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                // Optionally add Interval Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Repeat, contentDescription = "Interval", modifier = Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    val intervalText = buildString {
                        reminder?.timeInterval?.let { append("$it days") }
                        if(reminder?.mileageInterval != null && reminder.timeInterval > 0) append(" / ")
                        reminder?.mileageInterval?.let { append("$it mi") }
                        if (isEmpty()) append("N/A")
                    }
                    Text(
                        text = "Interval: $intervalText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


            }

            // --- Trailing Content (Optional): Last Service Info ---
            if (reminder?.lastDateCheck != null || reminder?.lastMileageCheck != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Last:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    Spacer(modifier = Modifier.height(2.dp))
                    // Last Date Check
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(Icons.Filled.EventAvailable, contentDescription = "Last Checked Date", modifier = Modifier.size(12.dp).padding(end=2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = reminder.lastDateCheck.take(10),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    // Last Mileage Check
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(Icons.Filled.Speed, contentDescription = "Last Checked Mileage", modifier = Modifier.size(12.dp).padding(end=2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${reminder.lastMileageCheck.toInt()} mi",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}