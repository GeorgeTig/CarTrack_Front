package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.BodyInfoDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection // Import if moved
import com.example.cartrack.feature.addvehicle.presentation.displayString
import androidx.compose.material3.MaterialTheme


@Composable
internal fun BodyStepContent(
    uiState: AddVehicleUiState,
    onSelectBody: (BodyInfoDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.availableBodies.isEmpty() && !uiState.isLoading) {
            Text("No specific body style details found for this configuration.")
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            DropdownSelection(
                label = "Select Body Style",
                options = uiState.availableBodies,
                selectedOption = uiState.confirmedBody,
                onOptionSelected = onSelectBody,
                optionToString = { it.displayString() },
                isEnabled = !uiState.isLoading,
                showRequiredMarker = uiState.needsBodyConfirmation && uiState.confirmedBody == null
            )
        }

        AnimatedVisibility(visible = uiState.confirmedBody != null) {
            Text(
                "Selected: ${uiState.confirmedBody.displayString()}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}