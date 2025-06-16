package com.example.cartrack.features.add_maintenance.helpers

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun MaintenanceDateField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    isEnabled: Boolean
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    try {
        val date = LocalDate.parse(selectedDate)
        calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
    } catch (_: Exception) {  }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val newDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(newDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }, year, month, day
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    Box {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Date of Service*") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") },
            trailingIcon = { Icon(Icons.Filled.ExpandMore, "Open Date Picker") },
            readOnly = true,
            enabled = isEnabled
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = isEnabled, onClick = { datePickerDialog.show() })
        )
    }
}

@Composable
fun MaintenanceOptionalInfoSection(
    serviceProvider: String,
    onServiceProviderChange: (String) -> Unit,
    cost: String,
    onCostChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isEnabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            label = { Text("Total Cost ($)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            enabled = isEnabled
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes / Comments") },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            maxLines = 5,
            enabled = isEnabled
        )
    }
}