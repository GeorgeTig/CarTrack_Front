package com.example.cartrack.features.car_history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private fun formatDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        try {
            ZonedDateTime.parse(dateString).format(formatter)
        } catch (e: DateTimeParseException) {
            LocalDate.parse(dateString).format(formatter)
        }
    } catch (e: DateTimeParseException) {
        "Invalid Date"
    }
}

private fun formatCurrency(cost: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(cost)
}

@Composable
fun HistoryEventCard(event: MaintenanceLogResponseDto, modifier: Modifier = Modifier) {
    // --- AICI ESTE CORECȚIA PRINCIPALĂ ---
    // Definim culorile direct în contextul Composable
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary
    val warningColor = Color(0xFFFFA000)

    // Blocul remember acum nu mai conține apeluri @Composable
    val eventTypeInfo = remember(event.entryType) {
        when {
            event.entryType.equals("MileageUpdate", ignoreCase = true) ->
                EventTypeInfo(
                    title = "Mileage Update",
                    icon = Icons.Default.Speed,
                    color = tertiaryColor
                )
            event.entryType.equals("Scheduled", ignoreCase = true) ->
                EventTypeInfo(
                    title = "Scheduled Maintenance",
                    icon = Icons.Default.EventAvailable,
                    color = primaryColor
                )
            else ->
                EventTypeInfo(
                    title = "Unscheduled Service",
                    icon = Icons.Default.Build,
                    color = warningColor
                )
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TimelineMarker(icon = eventTypeInfo.icon, color = eventTypeInfo.color)
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = eventTypeInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = eventTypeInfo.color
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatDate(event.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                if (event.entryType.equals("MileageUpdate", ignoreCase = true)) {
                    MileageUpdateContent(event)
                } else {
                    MaintenanceContent(event)
                }
            }
        }
    }
}

@Composable
private fun TimelineMarker(icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = color
            )
        }
    }
}

@Composable
private fun MaintenanceContent(event: MaintenanceLogResponseDto) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (event.performedTasks.isNotEmpty()) {
            event.performedTasks.forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Task done",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(task, style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Text("General service performed.", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
        }

        if (event.cost != null || !event.serviceProvider.isNullOrBlank()) {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
        ) {
            InfoChip(text = "${event.mileage.toInt()} mi", icon = Icons.Default.Speed)
            if (event.cost != null && event.cost > 0) {
                InfoChip(text = formatCurrency(event.cost), icon = Icons.Default.AttachMoney)
            }
        }

        if (!event.notes.isNullOrBlank() || !event.serviceProvider.isNullOrBlank()) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                event.serviceProvider?.let {
                    if (it.isNotBlank()) {
                        Text(
                            "Serviced at: $it",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                event.notes?.let {
                    if (it.isNotBlank()) {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MileageUpdateContent(event: MaintenanceLogResponseDto) {
    Column {
        Text("Odometer reading updated to ${event.mileage.toInt()} mi.", style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoChip(text: String, icon: ImageVector) {
    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

private data class EventTypeInfo(val title: String, val icon: ImageVector, val color: Color)