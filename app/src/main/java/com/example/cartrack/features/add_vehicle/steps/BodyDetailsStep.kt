package com.example.cartrack.features.add_vehicle.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Weekend
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
fun BodyDetailsStep(
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

        DropdownSelection(
            label = "Body Type*",
            options = uiState.availableBodyTypes,
            selectedOption = uiState.selectedBodyType,
            onOptionSelected = { onBodyTypeSelected(it) },
            optionToString = { it },
            isEnabled = !isLoading && uiState.availableBodyTypes.isNotEmpty(),
            isError = uiState.hasAttemptedNext && uiState.selectedBodyType == null,
            errorText = "Body type required"
        )

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