package com.example.cartrack.feature.editreminder.presentation.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun IntervalsEditForm(
    mileageInterval: String,
    onMileageChange: (String) -> Unit,
    mileageError: String?,
    timeInterval: String,
    onTimeChange: (String) -> Unit,
    timeError: String?
) {
    // Column-ul principal care conține cele două secțiuni
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp) // Spațiu mai mare între secțiuni
    ) {
        // --- Secțiunea pentru Mileage Interval ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subtitlu
            Text(
                "Mileage Interval",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Câmp de text cu indent
            OutlinedTextField(
                value = mileageInterval,
                onValueChange = onMileageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp), // AICI ESTE INDENT-UL
                label = { Text("Distance in miles (e.g., 5000)") }, // Am ajustat textul
                placeholder = { Text("Leave blank if not applicable") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                isError = mileageError != null,
                supportingText = {
                    mileageError?.let { Text(it) }
                },
                shape = MaterialTheme.shapes.medium
            )
        }

        // --- Secțiunea pentru Time Interval ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Subtitlu
            Text(
                "Time Interval",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Câmp de text cu indent
            OutlinedTextField(
                value = timeInterval,
                onValueChange = onTimeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp), // AICI ESTE INDENT-UL
                label = { Text("Period in days* (e.g., 180)") }, // Am ajustat textul
                placeholder = { Text("e.g., 180") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = timeError != null,
                supportingText = {
                    timeError?.let { Text(it) }
                },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}