package com.example.cartrack.features.add_maintenance.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.features.add_maintenance.SelectableReminder

@Composable
fun SelectableRemindersSection(
    reminders: List<SelectableReminder>,
    onReminderToggled: (Int, Boolean) -> Unit,
    isEnabled: Boolean
) {
    if (reminders.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Performed Scheduled Maintenance", style = MaterialTheme.typography.titleLarge)
            Text(
                "Select the maintenance tasks you have completed. Overdue items are pre-selected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            reminders.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isEnabled) { onReminderToggled(item.reminder.configId, !item.isSelected) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isSelected,
                        onCheckedChange = { isChecked -> onReminderToggled(item.reminder.configId, isChecked) },
                        enabled = isEnabled
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item.reminder.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}