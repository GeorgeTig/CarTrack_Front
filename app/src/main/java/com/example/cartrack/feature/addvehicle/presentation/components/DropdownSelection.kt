package com.example.cartrack.feature.addvehicle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> DropdownSelection(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionToString: (T) -> String, // Funcție pentru a converti opțiunea T în String pentru afișare
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true, // Adăugat default true
    isError: Boolean = false,
    errorText: String? = null,
    placeholderText: String = "Select..." // Placeholder mai generic
) {
    var expanded by remember { mutableStateOf(false) }
    // Afișează textul opțiunii selectate sau placeholder-ul dacă nimic nu e selectat
    val currentSelectionText = selectedOption?.let { optionToString(it) } ?: ""

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }


    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled && options.isNotEmpty(),
        onExpandedChange = { if (isEnabled && options.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentSelectionText,
            onValueChange = {}, // Nu se editează direct
            readOnly = true,
            label = { Text(label) },
            placeholder = {
                Text(
                    when {
                        !isEnabled -> "N/A (Disabled)"
                        options.isEmpty() && selectedOption == null -> "No options" // Mai clar când nu sunt opțiuni deloc
                        else -> placeholderText
                    }
                )
            },
            trailingIcon = {
                if (isError && isEnabled) {
                    Icon(Icons.Filled.Error, "Error", tint = MaterialTheme.colorScheme.error)
                } else if (options.isNotEmpty() && isEnabled) { // Afișează săgeata doar dacă sunt opțiuni și e activat
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor() // Important pentru poziționarea corectă a meniului
                .fillMaxWidth()
                .clickable(
                    enabled = isEnabled && options.isNotEmpty(),
                    onClick = { if (options.isNotEmpty()) expanded = true }, // Deschide meniul la click
                    indication = null, // Elimină ripple dacă vrei
                    interactionSource = interactionSource
                )
                .clearAndSetSemantics {
                    contentDescription = "$label. Currently selected: ${selectedOption?.let{optionToString(it)} ?: placeholderText}. Click to change."
                },
            enabled = isEnabled,
            isError = isError,
            singleLine = true,
            supportingText = {
                if (isError && errorText != null) {
                    Text(errorText, color = MaterialTheme.colorScheme.error)
                }
            },
            shape = MaterialTheme.shapes.medium // Colțuri rotunjite
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize(true) // Meniul ia lățimea ancorei
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