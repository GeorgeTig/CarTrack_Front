package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.EngineInfoDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection
import com.example.cartrack.feature.addvehicle.presentation.displayString

@Composable
internal fun EngineStepContent(
    uiState: AddVehicleUiState,
    onSelectEngine: (EngineInfoDto) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown - always shown if options exist
        if (uiState.availableEngines.isEmpty() && !uiState.isLoading) {
            Text(
                "No specific engine details found/required for this model.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            DropdownSelection(
                label = "Select Engine",
                options = uiState.availableEngines,
                selectedOption = uiState.confirmedEngine,
                onOptionSelected = onSelectEngine,
                optionToString = { it.displayString() },
                isEnabled = !uiState.isLoading ,
                isError = uiState.availableEngines.size > 1 && uiState.confirmedEngine == null,
                errorText = if (uiState.availableEngines.size > 1 && uiState.confirmedEngine == null) "Please select an engine" else null
            )
        }

        // --- Show Details of Selected Engine ---
        AnimatedVisibility(
            visible = uiState.confirmedEngine != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Use a Card to display the selected details nicely
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        "Selected Engine:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Display details using DetailRow or simple Text
                    uiState.confirmedEngine?.let { engine ->
                        DetailRow("Type:", engine.engineType)
                        DetailRow("Size:", "${engine.size} L")
                        DetailRow("HP:", engine.horsepower.toString())
                       DetailRow("Drive:", engine.driveType)
                        DetailRow("Transmission:", engine.transmission)
                    }
                }
            }
        }
    }
}