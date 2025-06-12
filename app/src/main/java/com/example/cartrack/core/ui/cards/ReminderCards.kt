package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// --- FUNCȚII AJUTĂTOARE PENTRU FORMATUL DATELOR ---

private fun formatDueDateAsText(days: Int): String {
    return when {
        days > 1 -> "in $days days"
        days == 1 -> "Tomorrow"
        days == 0 -> "Today"
        days < -1 -> "${abs(days)} days overdue"
        days == -1 -> "Yesterday"
        else -> "N/A"
    }
}

private fun formatDueMileageAsText(mileage: Double): String {
    val mileageAsLong = mileage.toLong()

    return when {
        mileageAsLong >= 1 -> "in ${mileageAsLong} mi"
        else -> "Now"
    }
}

private fun getFriendlyStatus(reminder: ReminderResponseDto, statusInfo: ReminderStatusIcon?): String {
    if (!reminder.isActive) return "Inactive"
    return when (statusInfo) {
        is ReminderStatusIcon.UpToDate -> "Up to date"
        is ReminderStatusIcon.DueSoon -> "Due soon"
        is ReminderStatusIcon.Overdue -> "Overdue"
        else -> "Unknown"
    }
}

// --- ENUM-URI și SEALED CLASSES ---

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

// --- COMPONENTE UI REUTILIZABILE ---

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
                if (reminder.isActive) {
                    DueInfoRow(
                        dueMileage = reminder.dueMileage,
                        dueDate = reminder.dueDate,
                        mileageInterval = reminder.mileageInterval,
                        timeInterval = reminder.timeInterval
                    )
                }
            }
            if (reminder.isEditable) {
                Icon(Icons.Default.EditNote, "Editable", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

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

        // Secțiunea de detalii
        MiniCardSection(title = "Details", icon = Icons.AutoMirrored.Filled.ListAlt) {
            DetailRow("Type:", reminder.typeName)
            DetailRow("Status:", getFriendlyStatus(reminder, statusInfo))
            DetailRow("Reminder Active:", if (reminder.isActive) "Yes" else "No")
        }

        // Secțiunea de configurare
        MiniCardSection(title = "Configuration", icon = Icons.Filled.Tune) {
            DetailRow("Mileage Interval:", if (reminder.mileageInterval > 0) "${reminder.mileageInterval} mi" else "Not set")
            DetailRow("Time Interval:", if (reminder.timeInterval > 0) "${reminder.timeInterval} days" else "Not set")
        }

        // Secțiunea "Next Maintenance"
        MiniCardSection(title = "Next Maintenance", icon = Icons.Filled.EventAvailable) {
            if (reminder.isActive) {
                if (reminder.mileageInterval > 0) {
                    DetailRow("Due Mileage:", formatDueMileageAsText(reminder.dueMileage))
                }
                if (reminder.timeInterval > 0) {
                    DetailRow("Due Date:", formatDueDateAsText(reminder.dueDate))
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Reminder is inactive. No tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// --- COMPONENTE HELPER INTERNE (PRIVATE) ---

@Composable
private fun DueInfoRow(
    dueMileage: Double,
    dueDate: Int,
    mileageInterval: Int,
    timeInterval: Int
) {
    val showMileage = mileageInterval > 0
    val showTime = timeInterval > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showMileage) {
            InfoChip(
                text = formatDueMileageAsText(dueMileage),
                icon = Icons.Filled.Speed
            )
        }
        if (showTime) {
            InfoChip(
                text = formatDueDateAsText(dueDate),
                icon = Icons.Filled.Event
            )
        }
    }
}

@Composable
private fun InfoChip(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MiniCardSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                Icon(icon, title, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Divider()
            Spacer(Modifier.height(8.dp))
            Column { content() }
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