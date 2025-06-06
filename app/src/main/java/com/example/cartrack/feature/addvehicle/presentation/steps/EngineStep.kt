package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection

@Composable
internal fun EngineDetailsStep(
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

        // 1. Engine Size Dropdown
        DropdownSelection(
            label = "Engine Size (Liters)*",
            options = uiState.availableEngineSizes,
            selectedOption = uiState.selectedEngineSize,
            onOptionSelected = { onSizeSelected(it) }, // Permite null pentru resetare/neselectare
            optionToString = { "%.1f L".format(it) }, // Formatează cu o zecimală
            isEnabled = !isLoading && uiState.availableEngineSizes.isNotEmpty(),
            isError = uiState.availableEngineSizes.isNotEmpty() && uiState.selectedEngineSize == null && uiState.confirmedEngineId == null, // Eroare dacă sunt opțiuni și nimic selectat
            errorText = "Size required",
            placeholderText = if (uiState.availableEngineSizes.isEmpty() && !isLoading) "N/A for selected series/year" else "Select Size"
        )

        // 2. Engine Type (Fuel) Dropdown - apare după ce se alege mărimea
        AnimatedVisibility(visible = uiState.selectedEngineSize != null && uiState.availableEngineTypes.isNotEmpty()) {
            DropdownSelection(
                label = "Fuel Type*",
                options = uiState.availableEngineTypes,
                selectedOption = uiState.selectedEngineType,
                onOptionSelected = { onTypeSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableEngineTypes.isNotEmpty(),
                isError = uiState.availableEngineTypes.isNotEmpty() && uiState.selectedEngineType == null && uiState.confirmedEngineId == null,
                errorText = "Fuel type required"
            )
        }

        // 3. Transmission Dropdown - apare după ce se alege tipul
        AnimatedVisibility(visible = uiState.selectedEngineType != null && uiState.availableTransmissions.isNotEmpty()) {
            DropdownSelection(
                label = "Transmission*",
                options = uiState.availableTransmissions,
                selectedOption = uiState.selectedTransmission,
                onOptionSelected = { onTransmissionSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableTransmissions.isNotEmpty(),
                isError = uiState.availableTransmissions.isNotEmpty() && uiState.selectedTransmission == null && uiState.confirmedEngineId == null,
                errorText = "Transmission required"
            )
        }

        // 4. Drive Type Dropdown - apare după ce se alege transmisia
        AnimatedVisibility(visible = uiState.selectedTransmission != null && uiState.availableDriveTypes.isNotEmpty()) {
            DropdownSelection(
                label = "Drive Type*",
                options = uiState.availableDriveTypes,
                selectedOption = uiState.selectedDriveType,
                onOptionSelected = { onDriveTypeSelected(it) },
                optionToString = { it },
                isEnabled = !isLoading && uiState.availableDriveTypes.isNotEmpty(),
                isError = uiState.availableDriveTypes.isNotEmpty() && uiState.selectedDriveType == null && uiState.confirmedEngineId == null,
                errorText = "Drive type required"
            )
        }

        if (uiState.allDecodedOptions.isNotEmpty() &&
            uiState.selectedSeriesName != null && uiState.selectedYear != null &&
            uiState.availableEngineSizes.isEmpty() && !isLoading) {
            Text("No specific engine configurations found from VIN data for the selected series/year. You might need to confirm details if proceeding.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}