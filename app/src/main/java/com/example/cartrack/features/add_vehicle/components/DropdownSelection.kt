package com.example.cartrack.features.add_vehicle.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelection(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    placeholderText: String = "Select..."
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectionText = selectedOption?.let { optionToString(it) } ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled && options.isNotEmpty(),
        onExpandedChange = { if (isEnabled && options.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentSelectionText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholderText) },
            trailingIcon = {
                if (isError && isEnabled) {
                    Icon(Icons.Filled.Error, "Error", tint = MaterialTheme.colorScheme.error)
                } else if (isEnabled && options.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clickable(
                    enabled = isEnabled && options.isNotEmpty(),
                    onClick = { expanded = true },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .clearAndSetSemantics {
                    contentDescription = "$label. Currently selected: ${selectedOption?.let { optionToString(it) } ?: placeholderText}. Click to change."
                },
            enabled = isEnabled,
            isError = isError,
            singleLine = true,
            supportingText = {
                if (isError && errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = MaterialTheme.shapes.medium
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