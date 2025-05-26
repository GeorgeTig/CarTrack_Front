package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.maintenance.presentation.EditReminderFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderDialog(
    formState: EditReminderFormState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onMileageIntervalChange: (String) -> Unit,
    onTimeIntervalChange: (String) -> Unit,
    onSave: () -> Unit,
    onRestoreDefaults: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Reminder: ${formState.reminderToEdit?.name ?: ""}", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()), // Make content scrollable
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Mileage Interval
                OutlinedTextField(
                    value = formState.mileageIntervalInput,
                    onValueChange = onMileageIntervalChange,
                    label = { Text("Mileage Interval (e.g., 5000)") },
                    placeholder = { Text("Leave blank if not applicable") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.mileageIntervalError != null,
                    supportingText = { formState.mileageIntervalError?.let { Text(it) } }
                )

                // Time Interval
                OutlinedTextField(
                    value = formState.timeIntervalInput,
                    onValueChange = onTimeIntervalChange,
                    label = { Text("Time Interval (e.g., 180 for days)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.timeIntervalError != null,
                    supportingText = { formState.timeIntervalError?.let { Text(it) } }
                )
                // Add other editable fields from your DTO if needed (e.g., isEditable toggle)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSave) { Text("Save") }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Restore to Defaults Button
                TextButton(
                    onClick = onRestoreDefaults,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Restore, contentDescription = "Restore", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Defaults")
                    }
                }
                // Cancel Button
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        // Customize shape, colors etc. for the dialog as needed
        shape = MaterialTheme.shapes.large
    )
}