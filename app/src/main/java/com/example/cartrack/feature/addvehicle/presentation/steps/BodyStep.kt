package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.BodyInfoDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow // Import DetailRow
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection
import com.example.cartrack.feature.addvehicle.presentation.displayString

@Composable
internal fun BodyStepContent(
    uiState: AddVehicleUiState,
    onSelectBody: (BodyInfoDto) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
    ) {
        // Dropdown
        if (uiState.availableBodies.isEmpty() && !uiState.isLoading) {
            Text(
                "No specific body details found/required for this model.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            DropdownSelection(
                label = "Select Body Style",
                options = uiState.availableBodies,
                selectedOption = uiState.confirmedBody,
                onOptionSelected = onSelectBody,
                optionToString = { it.displayString() },
                isEnabled = !uiState.isLoading,
                isError = uiState.availableBodies.size > 1 && uiState.confirmedBody == null,
                errorText = if (uiState.availableBodies.size > 1 && uiState.confirmedBody == null) "Please select a body style" else null
            )
        }

        // --- Show Details of Selected Body ---
        AnimatedVisibility(
            visible = uiState.confirmedBody != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        "Selected Body:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    uiState.confirmedBody?.let { body ->
                        DetailRow("Type:", body.bodyType)
                        DetailRow("Doors:", body.doorNumber.toString())
                        DetailRow("Seats:", body.seatNumber.toString())
                    }
                }
            }
        }
    }
}