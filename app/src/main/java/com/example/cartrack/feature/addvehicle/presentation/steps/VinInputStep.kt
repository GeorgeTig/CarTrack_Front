package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
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
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )

        Text(
            "Enter your vehicle's 17-character VIN to automatically fetch its details.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = uiState.vinInput,
            onValueChange = onVinChange,
            label = { Text("VIN Number") },
            placeholder = { Text("e.g., 1HG...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrect = false,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = uiState.vinValidationError != null,
            supportingText = {
                if (uiState.vinValidationError != null) {
                    Text(uiState.vinValidationError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        "${uiState.vinInput.length}/17 characters",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            enabled = !uiState.isLoading,
        )
    }
}