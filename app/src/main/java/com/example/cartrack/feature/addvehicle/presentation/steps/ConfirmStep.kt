package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.displayString
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow // Import if moved

@Composable
internal fun ConfirmStepContent(
    uiState: AddVehicleUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Please review the vehicle details below before saving.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("VIN:", uiState.vinInput)
                DetailRow("Make:", uiState.selectedProducer ?: "N/A")
                DetailRow("Series:", uiState.selectedSeriesDto?.seriesName ?: "N/A")
                DetailRow("Engine:", uiState.confirmedEngine.displayString())
                DetailRow("Body:", uiState.confirmedBody.displayString())
                DetailRow("Mileage:", uiState.mileageInput.ifBlank { "N/A" })

                // Conditionally show final model info
                val finalModel = uiState.selectedModelId?.let { id -> uiState.availableModels.find { it.modelId == id } }
                if (uiState.needsModelSelection && finalModel != null) {
                    // Display more specific details if needed/available in finalModel DTO
                    DetailRow("Specific Model:", "(ID: ${finalModel.modelId})") // Example
                } else if (!uiState.needsModelSelection && uiState.selectedModelId != null) {
                    // If auto-selected
                    DetailRow("Specific Model ID:", uiState.selectedModelId.toString())
                }
            }
        }
    }
}