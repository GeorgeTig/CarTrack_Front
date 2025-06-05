package com.example.cartrack.feature.addmaintenance.presentation.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MaintenanceGeneralInfoSection(
    date: String,
    dateError: String?,
    onDateChange: (String) -> Unit,
    mileage: String,
    mileageError: String?,
    onMileageChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MaintenanceDateField( // Presupunem că și acest Composable e în același pachet helpers
            selectedDate = date,
            onDateSelected = onDateChange,
            dateError = dateError
        )
        OutlinedTextField(
            value = mileage,
            onValueChange = onMileageChange,
            label = { Text("Current Mileage (km)*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = mileageError != null,
            supportingText = { mileageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(12.dp)
        )
    }
}