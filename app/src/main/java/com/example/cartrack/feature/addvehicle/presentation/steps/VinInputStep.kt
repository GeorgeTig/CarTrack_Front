package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun VinInputStep(
    vin: String,
    onVinChange: (String) -> Unit,
    vinError: String?,
    isLoading: Boolean, // Pentru spinner-ul de decodare
    onSkipToManual: () -> Unit, // Callback pentru skip
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp), // Padding redus sus/jos
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.VpnKey,
            contentDescription = "VIN Input",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Text(
            "Enter Vehicle Identification Number",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            "We'll try to fetch details automatically. You can also skip to enter them manually.",
            style = MaterialTheme.typography.bodySmall, // Font mai mic pentru explicație
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        OutlinedTextField(
            value = vin,
            onValueChange = onVinChange,
            label = { Text("VIN* (17 characters)") },
            placeholder = { Text("e.g., 1HG...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrect = false,
                imeAction = ImeAction.Done // Utilizatorul apasă Done, apoi Next din BottomBar
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = vinError != null,
            supportingText = {
                if (vinError != null) {
                    Text(vinError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        "${vin.length}/17",
                        color = if (vin.length == 17) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            enabled = !isLoading, // Dezactivat în timpul decodării
            shape = MaterialTheme.shapes.medium
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
        } else {
            TextButton(
                onClick = onSkipToManual,
                modifier = Modifier.padding(top = 0.dp) // Padding redus
            ) {
                Text("Enter Details Manually Instead")
            }
        }
    }
}