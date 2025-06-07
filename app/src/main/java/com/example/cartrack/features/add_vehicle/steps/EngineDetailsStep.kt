package com.example.cartrack.features.add_vehicle.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.features.add_vehicle.AddVehicleUiState
import com.example.cartrack.features.add_vehicle.components.DropdownSelection

@Composable
fun EngineDetailsStep(
    uiState: AddVehicleUiState,
    onSizeSelected: (Double?) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onTransmissionSelected: (String?) -> Unit,
    onDriveTypeSelected: (String?) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = "Engine Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Engine Specifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        DropdownSelection(
            label = "Engine Size (Liters)*",
            options = uiState.availableEngineSizes,
            selectedOption = uiState.selectedEngineSize,
            onOptionSelected = { onSizeSelected(it) },
            optionToString = { "%.1f L".format(it) },
            isEnabled = !isLoading && uiState.availableEngineSizes.isNotEmpty(),
            isError = uiState.hasAttemptedNext && uiState.selectedEngineSize == null,
            errorText = "Size required"
        )

        AnimatedVisibility(visible = uiState.selectedEngineSize != null) {
            DropdownSelection(
                label = "Fuel Type*",
                options = uiState.availableEngineTypes,
                selectedOption = uiState.selectedEngineType,
                onOptionSelected = { onTypeSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableEngineTypes.isNotEmpty(),
                isError = uiState.hasAttemptedNext && uiState.selectedEngineType == null,
                errorText = "Fuel type required"
            )
        }

        AnimatedVisibility(visible = uiState.selectedEngineType != null) {
            DropdownSelection(
                label = "Transmission*",
                options = uiState.availableTransmissions,
                selectedOption = uiState.selectedTransmission,
                onOptionSelected = { onTransmissionSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableTransmissions.isNotEmpty(),
                isError = uiState.hasAttemptedNext && uiState.selectedTransmission == null,
                errorText = "Transmission required"
            )
        }

        AnimatedVisibility(visible = uiState.selectedTransmission != null) {
            DropdownSelection(
                label = "Drive Type*",
                options = uiState.availableDriveTypes,
                selectedOption = uiState.selectedDriveType,
                onOptionSelected = { onDriveTypeSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableDriveTypes.isNotEmpty(),
                isError = uiState.hasAttemptedNext && uiState.selectedDriveType == null,
                errorText = "Drive type required"
            )
        }
    }
}