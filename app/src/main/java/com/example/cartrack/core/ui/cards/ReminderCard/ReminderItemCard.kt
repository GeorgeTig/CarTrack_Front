package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.foundation.clickable // Added for making the card clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    reminder: ReminderResponseDto?,
    onClick: () -> Unit // Callback for when the card is clicked
) {
    val typeIconInfo = MaintenanceTypeIcon.fromTypeId(reminder?.typeId)
    val statusIconInfo = ReminderStatusIcon.fromStatusId(reminder?.statusId)

    Card(
        onClick = { if (reminder != null) onClick() }, // Make card clickable only if reminder data exists
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Increased elevation slightly for better click affordance
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
                modifier = Modifier.size(36.dp).padding(end = 10.dp), // Slightly smaller icon
                tint = MaterialTheme.colorScheme.primary
            )

            // --- Middle Content: Name, Status Icon, Due Info ---
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder?.name ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium, // Keep title medium for name
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp)
                    )
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

                // Due Date Info (Concise)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, contentDescription = "Due Date", modifier = Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Due: ${reminder?.dueDate?.take(10) ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }

                // Due Mileage Info (Concise)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, contentDescription = "Due Mileage", modifier = Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "At: ${reminder?.dueMileage?.toInt() ?: "-"} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                // Interval info removed for summary card - will be in detail card
            }

            // Trailing content (isEditable flag - subtle icon)
            if (reminder?.isEditable == true) { // Show if editable
                Icon(
                    imageVector = Icons.Filled.EditNote, // Or Edit
                    contentDescription = "Editable",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp).padding(start = 8.dp)
                )
            }
            // Last check info removed for summary card - will be in detail card
        }
    }
}