package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto

@Composable
fun ActivateReminderDialog(
    reminder: ReminderResponseDto,
    onDismiss: () -> Unit,
    onConfirmActivate: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = "Activate Reminder") },
        title = {
            Text(
                text = "Activate Reminder?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text("Do you want to activate the reminder for '${reminder.name}'?")
                Text("Type: ${reminder.typeName}", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmActivate,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Yes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("No")
            }
        },
        shape = MaterialTheme.shapes.large
    )
}