package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.EngineInfoDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection // Import if moved
import com.example.cartrack.feature.addvehicle.presentation.displayString
import androidx.compose.material3.MaterialTheme

@Composable
internal fun EngineStepContent(
    uiState: AddVehicleUiState,
    onSelectEngine: (EngineInfoDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.availableEngines.isEmpty() && !uiState.isLoading) {
            Text("No specific engine details found for this model year.")
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            DropdownSelection(
                label = "Select Engine Details",
                options = uiState.availableEngines,
                selectedOption = uiState.confirmedEngine,
                onOptionSelected = onSelectEngine,
                optionToString = { it.displayString() },
                isEnabled = !uiState.isLoading,
                showRequiredMarker = uiState.needsEngineConfirmation && uiState.confirmedEngine == null
            )
        }

        AnimatedVisibility(visible = uiState.confirmedEngine != null) {
            Text(
                "Selected: ${uiState.confirmedEngine.displayString()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}