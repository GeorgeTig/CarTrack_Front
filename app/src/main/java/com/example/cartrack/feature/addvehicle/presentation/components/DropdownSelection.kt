package com.example.cartrack.feature.addvehicle.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Make internal or public
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> DropdownSelection(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    showRequiredMarker: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedOption?.let { optionToString(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled && options.isNotEmpty(),
        onExpandedChange = { if (isEnabled && options.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { if (options.isEmpty() && isEnabled) Text("No options available") else Text("Select...") },
            trailingIcon = { if (options.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled,
            isError = showRequiredMarker && selectedText.isEmpty(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(true)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionToString(option), style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}