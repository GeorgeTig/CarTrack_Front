package com.example.cartrack.features.add_maintenance.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MaintenanceGeneralInfoSection(
    date: String,
    dateError: String?,
    onDateChange: (String) -> Unit,
    mileage: String,
    mileageError: String?,
    onMileageChange: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MaintenanceDateField(
            selectedDate = date,
            onDateSelected = onDateChange,
            dateError = dateError,
            isEnabled = isEnabled
        )
        OutlinedTextField(
            value = mileage,
            onValueChange = onMileageChange,
            label = { Text("Current Mileage (mi)*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = mileageError != null,
            supportingText = { mileageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            enabled = isEnabled
        )
    }
}

@Composable
fun MaintenanceOptionalInfoSection(
    serviceProvider: String,
    onServiceProviderChange: (String) -> Unit,
    cost: String,
    costError: String?,
    onCostChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isEnabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = serviceProvider,
            onValueChange = onServiceProviderChange,
            label = { Text("Service Provider") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            enabled = isEnabled
        )
        OutlinedTextField(
            value = cost,
            onValueChange = onCostChange,
            label = { Text("Total Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            isError = costError != null,
            supportingText = { costError?.let { Text(it) } },
            enabled = isEnabled
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes / Comments") },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            maxLines = 5,
            enabled = isEnabled
        )
    }
}