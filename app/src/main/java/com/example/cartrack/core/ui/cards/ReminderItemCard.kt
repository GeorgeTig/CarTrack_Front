package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PauseCircleOutline
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
import kotlin.math.abs

// --- FUNCȚIE AJUTĂTOARE PENTRU FORMATUL ZILELOR ---
private fun formatDueDateAsText(days: Int): String {
    return when {
        days > 1 -> "in $days days"
        days == 1 -> "Tomorrow"
        days == 0 -> "Today"
        days == -1 -> "Yesterday"
        days < -1 -> "${abs(days)} days overdue"
        else -> "N/A"
    }
}

// --- ENUM-URI și SEALED CLASSES PENTRU UI ---

enum class MaintenanceTypeIcon(val icon: ImageVector) {
    OIL(Icons.Filled.OilBarrel), FLUIDS(Icons.Filled.Opacity), FILTERS(Icons.Filled.FilterAlt),
    BRAKES(Icons.Filled.DiscFull), ELECTRICAL(Icons.Filled.ElectricalServices), SUSPENSION(Icons.Filled.Compress),
    TRANSMISSION(Icons.Filled.Settings), STEERING(Icons.Filled.DirectionsCar), CONDITIONING(Icons.Filled.AcUnit),
    OTHER(Icons.Filled.Build);

    companion object {
        fun fromTypeId(typeId: Int?): MaintenanceTypeIcon = when (typeId) {
            1 -> OIL; 2 -> FLUIDS; 3 -> FILTERS; 4 -> BRAKES; 5 -> ELECTRICAL; 6 -> SUSPENSION;
            7 -> TRANSMISSION; 8 -> STEERING; 9 -> CONDITIONING; else -> OTHER
        }
    }
}

sealed class ReminderStatusIcon(val icon: ImageVector, val color: @Composable () -> Color) {
    data object UpToDate : ReminderStatusIcon(Icons.Filled.CheckCircle, { Color(0xFF4CAF50) })
    data object DueSoon : ReminderStatusIcon(Icons.Filled.Warning, { MaterialTheme.colorScheme.tertiary })
    data object Overdue : ReminderStatusIcon(Icons.Filled.Error, { MaterialTheme.colorScheme.error })
    data object IsInactive : ReminderStatusIcon(Icons.Outlined.PauseCircleOutline, { MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) })

    companion object {
        fun from(isActive: Boolean, statusId: Int?): ReminderStatusIcon? = when {
            !isActive -> IsInactive
            statusId == 1 -> UpToDate
            statusId == 2 -> DueSoon
            statusId == 3 -> Overdue
            else -> null
        }
    }
}

// --- CARDUL PENTRU LISTE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemCard(
    reminder: ReminderResponseDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeIcon = MaintenanceTypeIcon.fromTypeId(reminder.typeId)
    val statusInfo = ReminderStatusIcon.from(reminder.isActive, reminder.statusId)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = typeIcon.icon,
                contentDescription = reminder.typeName,
                modifier = Modifier.size(40.dp).padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp)
                    )
                    statusInfo?.let {
                        Icon(it.icon, "Status", tint = it.color(), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.Event, "Due Date", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)

                    // --- AICI ESTE CORECȚIA PRINCIPALĂ ---
                    Text(
                        text = "Due: ${formatDueDateAsText(reminder.dueDate)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    // --- SFÂRȘIT CORECȚIE ---

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.Speed, "Due Mileage", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("At: ${reminder.dueMileage.toInt()} mi", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (reminder.isEditable) {
                Icon(Icons.Default.EditNote, "Editable", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}