package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.displayString
import com.example.cartrack.feature.addvehicle.presentation.getFinalConfirmedModelDetailsForDisplay
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

@Composable
internal fun ConfirmStepContent(
    uiState: AddVehicleUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Please review the vehicle details below before saving.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DetailRow("VIN:", uiState.vinInput)
                // Display combined Make/Series/Year using helper
                DetailRow("Vehicle:", uiState.getFinalConfirmedModelDetailsForDisplay().ifBlank { "N/A" })
                DetailRow("Engine:", uiState.confirmedEngine.displayString())
                DetailRow("Body:", uiState.confirmedBody.displayString())
                DetailRow("Mileage:", uiState.mileageInput.ifBlank { "N/A" })

                // Display Final Model ID only if it was explicitly selected or auto-selected
                if (uiState.selectedModelId != null) {
                    DetailRow("Model ID:", uiState.selectedModelId.toString())
                }
            }
        }
        // Add a note about saving (optional)
        Text(
            "Click 'Save Vehicle' to add this car to your garage.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}