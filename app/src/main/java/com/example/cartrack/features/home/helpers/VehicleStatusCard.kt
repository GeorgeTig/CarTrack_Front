package com.example.cartrack.features.home.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.ui.cards.ReminderStatusIcon

@Composable
fun VehicleStatusCard(
    warnings: List<ReminderResponseDto>,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onWarningClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val overallStatus = when {
        warnings.any { it.statusId == 3 } -> ReminderStatusIcon.Overdue
        warnings.any { it.statusId == 2 } -> ReminderStatusIcon.DueSoon
        else -> ReminderStatusIcon.UpToDate
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpansion)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = overallStatus.icon,
                    contentDescription = "Overall Status",
                    tint = overallStatus.color(),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Vehicle Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                StatusChip(overallStatus, warnings.size)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    HorizontalDivider()
                    if (warnings.isEmpty()) {
                        Text(
                            "No active warnings. Your vehicle is up to date.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        warnings.forEach { warning ->
                            WarningItemRow(reminder = warning, onClick = { onWarningClick(warning.configId) })
                            if (warning != warnings.last()) {
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusChip(status: ReminderStatusIcon, warningCount: Int) {
    val text = if (status is ReminderStatusIcon.UpToDate) "All Good" else "$warningCount Warnings"
    val chipColor = status.color()

    SuggestionChip(
        onClick = {  },
        label = { Text(text, fontWeight = FontWeight.SemiBold) },
        icon = {
            Icon(
                status.icon,
                null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = chipColor.copy(alpha = 0.15f),
            labelColor = chipColor
        ),
        border = null
    )
}

@Composable
private fun WarningItemRow(reminder: ReminderResponseDto, onClick: () -> Unit) {
    val status = ReminderStatusIcon.from(reminder.isActive, reminder.statusId)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        status?.let {
            Icon(it.icon, null, tint = it.color(), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(reminder.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}