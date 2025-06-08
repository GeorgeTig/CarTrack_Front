package com.example.cartrack.features.car_history.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun HistoryEventCard(event: MaintenanceLogResponseDto, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, "Maintenance Event", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Maintenance Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = try {
                        val instant = Instant.parse(event.date)
                        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        "${dt.dayOfMonth} ${dt.month.name.take(3)} ${dt.year}"
                    } catch (e: Exception) { event.date.take(10) },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // Detalii
            Text("Performed Tasks:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Column(Modifier.padding(start = 16.dp, top = 4.dp)) {
                event.performedTasks.forEach { task ->
                    Text("• $task", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                InfoChip(label = "${event.mileage.toInt()} mi", icon = Icons.Default.Speed)
                if(event.cost > 0) InfoChip(label = "$${event.cost}", icon = Icons.Default.Money)
            }

            if (event.notes.isNotBlank()) {
                OutlinedCard(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Notes, null, Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, icon: ImageVector? = null) {
    SuggestionChip(
        onClick = {},
        label = { Text(label) },
        icon = {
            if(icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}