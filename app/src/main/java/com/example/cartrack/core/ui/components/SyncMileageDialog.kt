package com.example.cartrack.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SyncMileageDialog(
    currentMileage: Double?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var mileageInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    val isConfirmEnabled = mileageInput.isNotBlank()

    fun validateAndConfirm() {
        val newMileage = mileageInput.toDoubleOrNull()
        if (newMileage != null && currentMileage != null && newMileage <= currentMileage) {
            inputError = "Must be greater than ${currentMileage.toInt()} km"
        } else {
            inputError = null
            onConfirm(mileageInput)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Mileage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentMileage != null) {
                    Text(
                        "Current Odometer: ${currentMileage.toInt()} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = mileageInput,
                    onValueChange = {
                        mileageInput = it.filter { c -> c.isDigit() }
                        if (inputError != null) {
                            inputError = null
                        }
                    },
                    label = { Text("New Odometer (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = inputError != null,
                    supportingText = {
                        if (inputError != null) {
                            Text(inputError!!)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    validateAndConfirm()
                },
                enabled = isConfirmEnabled
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