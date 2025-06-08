package com.example.cartrack.features.car_history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryEventCard(event: MaintenanceLogResponseDto, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build, // Iconiță pentru mentenanță
                    contentDescription = "Maintenance Event",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Maintenance Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatDate(event.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // --- PERFpORMED TASKS ---
            Text("Performed Tasks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(start = 8.dp)) {
                if (event.performedTasks.isNotEmpty()) {
                    event.performedTasks.forEach { task ->
                        Text("• $task", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Text("No specific tasks listed.", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Spacer(Modifier.height(8.dp))

            // --- DETAILS (COST, MILEAGE, ETC) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailItem(icon = Icons.Default.Speed, label = "Mileage", value = "${event.mileage.toInt()} mi")
                DetailItem(icon = Icons.Default.AttachMoney, label = "Cost", value = formatCurrency(event.cost))
                if (event.serviceProvider.isNotBlank()) {
                    DetailItem(icon = Icons.Default.Store, label = "Provider", value = event.serviceProvider)
                }
            }

            // --- NOTES ---
            if (event.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Icon(Icons.Default.Notes, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text("Notes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Text(event.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --- Helper Functions ---
private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.dayOfMonth} ${dt.month.name.take(3).uppercase()} ${dt.year}"
    } catch (e: Exception) {
        "Unknown Date"
    }
}

private fun formatCurrency(cost: Double): String {
    return if (cost > 0) {
        // Formatează ca monedă, de ex: $123.45
        NumberFormat.getCurrencyInstance(Locale.US).format(cost)
    } else {
        "Unspecified"
    }
}