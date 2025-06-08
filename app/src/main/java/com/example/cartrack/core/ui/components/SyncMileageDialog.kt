package com.example.cartrack.core.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun SyncMileageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var mileageInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Mileage") },
        text = {
            OutlinedTextField(
                value = mileageInput,
                onValueChange = { mileageInput = it.filter { c -> c.isDigit() } },
                label = { Text("Current Odometer (mi)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(mileageInput) },
                enabled = mileageInput.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}