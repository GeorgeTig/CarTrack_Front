package com.example.cartrack.core.ui.cards.ReminderCard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto


@OptIn(ExperimentalMaterial3Api::class) // Needed for ListItem
@Composable
fun ReminderItemCard(
    modifier: Modifier = Modifier,
    reminder: ReminderResponseDto?
) {
    // Determine icons based on reminder data (or default/null if reminder is null)
    val categoryIcon = MaintenanceCategoryIcon.fromCategoryName(reminder?.maintenanceCategoryName)
    val statusIconInfo = ReminderStatusIcon.fromStatusId(reminder?.statusId)

    // Log for debugging
    LaunchedEffect(reminder?.configId) {
        Log.d("ReminderItemCard", "Rendering Card ID: ${reminder?.configId}, Status ID: ${reminder?.statusId}, Status Name: '${reminder?.statusName}', Found Icon Info: ${statusIconInfo != null}")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Slightly less elevation maybe
        // Use outlined variant for less emphasis if desired:
        // border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        // colors = CardDefaults.outlinedCardColors()
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Use a subtle surface color
        )
    ) {
        ListItem(
            // --- Leading Content: Category Icon ---
            leadingContent = {
                Icon(
                    imageVector = categoryIcon.icon,
                    contentDescription = categoryIcon.name,
                    modifier = Modifier.size(40.dp), // Keep size or adjust
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            // --- Headline Content: Reminder Name + Optional Status Icon ---
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder?.reminderName ?: "Loading...",
                        style = MaterialTheme.typography.titleMedium, // M3 uses titleMedium here
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1, // Keep it concise in the headline
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp) // Allow shrinking
                    )
                    // Optional Status/Warning Icon
                    statusIconInfo?.let { statusInfo ->
                        Icon(
                            imageVector = statusInfo.icon,
                            contentDescription = reminder?.statusName,
                            tint = statusInfo.tintColor(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            // --- Supporting Content: Type/Category and Due Info ---
            supportingContent = {
                Column {
                    // Type/Category Info (optional, can be subtle)
                    Text(
                        text = "${reminder?.maintenanceCategoryName ?: ""} - ${reminder?.maintenanceTypeName ?: ""}",
                        style = MaterialTheme.typography.bodySmall, // Use bodySmall for supporting text
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = LocalContentColor.current.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Due Info
                    val isOverdue = statusIconInfo is ReminderStatusIcon.Overdue
                    val isCompleted = statusIconInfo is ReminderStatusIcon.Completed
                    val dueDateInfo = when {
                        reminder == null -> "Loading info..."
                        isCompleted -> "Completed: ${reminder.lastDate?.take(10) ?: "N/A"}"
                        isOverdue -> "Service Overdue"
                        // TODO: Add logic for 'Due Soon' or actual next due date/mileage
                        else -> "Status: ${reminder.statusName}"
                    }
                    Text(
                        text = dueDateInfo,
                        style = MaterialTheme.typography.bodyMedium, // Use bodyMedium for main supporting line
                        color = statusIconInfo?.tintColor() ?: LocalContentColor.current // Use status color
                    )
                }
            },
            // --- Trailing Content: Last Service Info (Optional) ---
            // Example: Putting last service info on the trailing edge
            trailingContent = {
                if (reminder?.lastDate != null || reminder?.lastMileage != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = reminder.lastDate?.take(10) ?: "N/A",
                            style = MaterialTheme.typography.labelSmall // Smaller for trailing info
                        )
                        Text(
                            text = "${reminder.lastMileage?.toInt() ?: "-"} mi", // Format mileage
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            // Adjust ListItem colors if needed, though surfaceVariant card usually looks okay
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent // Make ListItem background transparent
            )
        ) // End ListItem
    } // End Card
}
