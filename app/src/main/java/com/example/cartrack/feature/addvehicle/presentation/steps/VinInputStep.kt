package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState

@Composable
internal fun VinInputStepContent(
    uiState: AddVehicleUiState,
    onVinChange: (String) -> Unit,
    onDecodeVin: () -> Unit // Still needed for clarity, triggered by Next button externally
) {
    val focusManager = LocalFocusManager.current
    // Wrap in column to ensure content is centered and laid out vertically
    Column(
        modifier = Modifier.fillMaxWidth(), // Fill width for proper alignment
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Enter your vehicle's 17-character VIN to automatically fetch its details.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.vinInput,
            onValueChange = onVinChange,
            label = { Text("VIN Number") },
            placeholder = { Text("e.g., 1HG...") },
            modifier = Modifier.fillMaxWidth(), // Take full width within the Column
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrect = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    // Logic moved to the central "Next" button handler in AddVehicleScreen
                }
            ),
            isError = uiState.vinValidationError != null,
            supportingText = {
                if (uiState.vinValidationError != null) {
                    Text(uiState.vinValidationError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("${uiState.vinInput.length}/17 characters")
                }
            },
            enabled = !uiState.isLoading
        )
    }
}