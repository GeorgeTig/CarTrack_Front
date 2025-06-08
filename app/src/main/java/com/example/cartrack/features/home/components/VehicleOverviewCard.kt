package com.example.cartrack.features.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleInfoResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto
import com.example.cartrack.features.home.helpers.VehicleStatusCard

@Composable
fun VehicleOverviewCard(
    vehicle: VehicleResponseDto,
    vehicleInfo: VehicleInfoResponseDto?,
    warnings: List<ReminderResponseDto>,
    isWarningsExpanded: Boolean,
    onToggleWarnings: () -> Unit,
    onWarningClick: (Int) -> Unit,
    onViewDetailsClick: () -> Unit,
    onLogServiceClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            // Partea 1: Statusul Vehiculului (expandabil)
            VehicleStatusCard(
                warnings = warnings,
                isExpanded = isWarningsExpanded,
                onToggleExpansion = onToggleWarnings,
                onWarningClick = onWarningClick
            )

            HorizontalDivider()

            // Partea 2: Detalii Esențiale
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Key Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DetailRow(label = "VIN", value = vehicle.vin)
                DetailRow(label = "Year", value = vehicle.year.toString())
                DetailRow(label = "Mileage", value = vehicleInfo?.mileage?.toInt()?.let { "$it mi" } ?: "N/A")
            }

            HorizontalDivider()

            // Partea 3: Acțiuni Rapide
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
                QuickActionItem(
                    icon = Icons.Default.CalendarViewMonth,
                    text = "View Full Details & Specs",
                    onClick = onViewDetailsClick
                )
                QuickActionItem(
                    icon = Icons.Default.EditCalendar,
                    text = "Log New Maintenance/Service",
                    onClick = onLogServiceClick
                )
                QuickActionItem(
                    icon = Icons.Default.AddRoad,
                    text = "View Service History",
                    onClick = onViewHistoryClick
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}