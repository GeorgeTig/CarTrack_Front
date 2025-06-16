package com.example.cartrack.features.edit_reminder.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    timeError: String?,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OutlinedTextField(
            value = mileageInterval,
            onValueChange = onMileageChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mileage Interval (km)") },
            placeholder = { Text("e.g., 5000 (optional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = mileageError != null,
            supportingText = { mileageError?.let { Text(it) } },
            enabled = isEnabled
        )

        OutlinedTextField(
            value = timeInterval,
            onValueChange = onTimeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Time Interval (days)*") },
            placeholder = { Text("e.g., 180") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true,
            isError = timeError != null,
            supportingText = { timeError?.let { Text(it) } },
            enabled = isEnabled
        )
    }
}