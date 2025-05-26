package com.example.cartrack.core.ui.cards.ReminderCard

import android.util.Log
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
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemCard(
    modifier: Modifier = Modifier,
    reminder: ReminderResponseDto?, // Use the NEW DTO
    onClick: () -> Unit
) {
    val typeIconInfo = MaintenanceTypeIcon.fromTypeId(reminder?.typeId)

    // --- Determine Status Icon based on isActive and statusId ---
    val finalStatusIconInfo = if (reminder?.isActive == false) {
        ReminderStatusIcon.fromActive()
    } else {
        ReminderStatusIcon.fromStatusId(reminder?.statusId) // Use for active warnings
    }

    LaunchedEffect(reminder?.configId) {
        Log.d("ReminderItemCard", "Render ID: ${reminder?.configId}, IsActive: ${reminder?.isActive}, StatusID: ${reminder?.statusId}, FinalIcon: $finalStatusIconInfo")
    }

    Card(
        onClick = { if (reminder != null) onClick() },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = typeIconInfo.icon,
                contentDescription = reminder?.typeName ?: "Maintenance Type",
                modifier = Modifier.size(36.dp).padding(end = 10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
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
                    // Use finalStatusIconInfo
                    finalStatusIconInfo?.let { statusInfo ->
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = "Status",
                            tint = statusInfo.tintColor(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Event, "Due Date", Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Due: ${reminder?.dueDate?.take(10) ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        // Color based on finalStatusIconInfo, default if null
                        color = finalStatusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, "Due Mileage", Modifier.size(14.dp).padding(end=4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "At: ${reminder?.dueMileage?.toInt() ?: "-"} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = finalStatusIconInfo?.tintColor() ?: MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (reminder?.isEditable == true) {
                Icon(
                    Icons.Filled.EditNote, "Editable",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp).padding(start = 8.dp)
                )
            }
        }
    }
}