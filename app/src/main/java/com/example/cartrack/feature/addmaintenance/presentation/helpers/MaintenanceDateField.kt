package com.example.cartrack.feature.addmaintenance.presentation.helpers

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun MaintenanceDateField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    dateError: String?
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Parsează data selectată pentru a inițializa DatePickerDialog
    // Folosește remember cu selectedDate ca cheie pentru a recalcula corect an, lună, zi
    val (initialYear, initialMonth, initialDay) = remember(selectedDate) {
        try {
            val parsedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
            Triple(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: Exception) {
            val cal = Calendar.getInstance()
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    val datePickerDialog = remember(context, initialYear, initialMonth, initialDay) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                focusManager.moveFocus(FocusDirection.Down) // Mută focusul la următorul câmp
            }, initialYear, initialMonth, initialDay
        ).apply {
            datePicker.maxDate = System.currentTimeMillis() // Nu permite selectarea datelor viitoare
        }
    }

    OutlinedTextField(
        value = selectedDate, // Afișează data formatată
        onValueChange = { /* Nu se editează manual */ },
        label = { Text("Date of Service*") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() },
        leadingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") },
        trailingIcon = { Icon(Icons.Filled.ExpandMore, "Open Date Picker") },
        readOnly = true,
        isError = dateError != null,
        supportingText = { dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        shape = RoundedCornerShape(12.dp)
    )
}
