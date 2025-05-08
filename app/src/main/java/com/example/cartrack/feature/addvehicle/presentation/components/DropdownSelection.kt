package com.example.cartrack.feature.addvehicle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

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
    isError: Boolean = false,
    errorText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedOption?.let { optionToString(it) } ?: ""
    val hasOptions = options.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled && hasOptions,
        onExpandedChange = { if (isEnabled && hasOptions) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = {
                if (!isEnabled) Text("N/A")
                else if (!hasOptions) Text("No options available")
                else Text("Select...")
            },
            trailingIcon = {
                if (isError && isEnabled) {
                    Icon(Icons.Filled.Error, "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (hasOptions && isEnabled) {
                   Icon(Icons.Filled.ArrowDropDown,
                       "Dropdown",
                       modifier = Modifier.clickable(enabled = isEnabled && hasOptions) { expanded = true })
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled,
            isError = isError,
            singleLine = true,
            supportingText = {
                if (isError && errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled && hasOptions,
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