package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.data.model.ModelDecodedDto // Import Model DTO
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection // Import if moved

@Composable
internal fun MileageStepContent(
    uiState: AddVehicleUiState,
    onMileageChange: (String) -> Unit,
    onSelectModel: (ModelDecodedDto) -> Unit // <-- ADDED PARAMETER
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Enter the current mileage (odometer reading) for your vehicle.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.mileageInput,
            onValueChange = onMileageChange,
            label = { Text("Current Mileage") },
            placeholder = { Text("e.g., 50000") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            isError = uiState.mileageValidationError != null,
            supportingText = {
                if (uiState.mileageValidationError != null) {
                    Text(uiState.mileageValidationError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Enter numbers only")
                }
            },
            enabled = !uiState.isLoading
        )

        // Optional final model selection dropdown
        AnimatedVisibility(visible = uiState.needsModelSelection && uiState.availableModels.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Multiple models match. Please select the specific one:",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                DropdownSelection(
                    label = "Select Final Specific Model",
                    options = uiState.availableModels,
                    selectedOption = uiState.selectedModelId?.let { id -> uiState.availableModels.find { it.modelId == id } },
                    onOptionSelected = onSelectModel, // <-- USE THE PARAMETER HERE
                    optionToString = { model ->
                        // Create a descriptive string - Adapt as needed
                        "${uiState.selectedProducer ?: ""} ${uiState.selectedSeriesDto?.seriesName ?: "Model"} (${model.year ?: "N/A"}) (ID: ${model.modelId})"
                    },
                    isEnabled = !uiState.isLoading,
                    showRequiredMarker = uiState.selectedModelId == null // Required if this dropdown is shown
                )
            }
        }
    }
}