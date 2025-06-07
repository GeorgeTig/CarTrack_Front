package com.example.cartrack.features.add_maintenance.helpers

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun MaintenanceDateField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    dateError: String?,
    isEnabled: Boolean
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val (year, month, day) = remember(selectedDate) {
        try {
            val parsedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
            Triple(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: Exception) {
            val cal = Calendar.getInstance()
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val localDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            focusManager.moveFocus(FocusDirection.Down)
        }, year, month, day
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text("Date of Service*") },
        modifier = Modifier.fillMaxWidth().clickable(enabled = isEnabled) { datePickerDialog.show() },
        leadingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") },
        trailingIcon = { Icon(Icons.Filled.ExpandMore, "Open Date Picker") },
        readOnly = true,
        isError = dateError != null,
        supportingText = { dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        enabled = isEnabled
    )
}