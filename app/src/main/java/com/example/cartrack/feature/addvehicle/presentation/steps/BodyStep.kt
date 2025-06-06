package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.BodyInfoDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection

@Composable
internal fun BodyDetailsStep(
    uiState: AddVehicleUiState,
    onBodyTypeSelected: (String?) -> Unit,
    onDoorNumberSelected: (Int?) -> Unit,
    onSeatNumberSelected: (Int?) -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Filled.Weekend, contentDescription = "Body Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Body Specifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        // 1. Body Type Dropdown
        DropdownSelection(
            label = "Body Type*",
            options = uiState.availableBodyTypes,
            selectedOption = uiState.selectedBodyType,
            onOptionSelected = { onBodyTypeSelected(it) },
            optionToString = { it },
            isEnabled = !isLoading && uiState.availableBodyTypes.isNotEmpty(),
            // MODIFICARE CHEIE: Condiția pentru eroare
            isError = uiState.hasAttemptedNext && uiState.selectedBodyType == null,
            errorText = "Body type required",
            placeholderText = if (uiState.availableBodyTypes.isEmpty() && !isLoading) "N/A" else "Select Body Type"
        )

        // 2. Door Number Dropdown
        AnimatedVisibility(visible = uiState.selectedBodyType != null) {
            DropdownSelection(
                label = "Number of Doors*",
                options = uiState.availableDoorNumbers,
                selectedOption = uiState.selectedDoorNumber,
                onOptionSelected = { onDoorNumberSelected(it) },
                optionToString = { it.toString() },
                isEnabled = !isLoading && uiState.availableDoorNumbers.isNotEmpty(),
                isError = uiState.hasAttemptedNext && uiState.selectedDoorNumber == null,
                errorText = "Door number required"
            )
        }

        // 3. Seat Number Dropdown
        AnimatedVisibility(visible = uiState.selectedDoorNumber != null) {
            DropdownSelection(
                label = "Number of Seats*",
                options = uiState.availableSeatNumbers,
                selectedOption = uiState.selectedSeatNumber,
                onOptionSelected = { onSeatNumberSelected(it) },
                optionToString = { it.toString() },
                isEnabled = !isLoading && uiState.availableSeatNumbers.isNotEmpty(),
                isError = uiState.hasAttemptedNext && uiState.selectedSeatNumber == null,
                errorText = "Seat number required"
            )
        }
    }
}