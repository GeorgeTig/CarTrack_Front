package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
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
import com.example.cartrack.feature.addvehicle.data.model.ModelDecodedDto
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection

@Composable
internal fun MileageStepContent(
    uiState: AddVehicleUiState,
    onMileageChange: (String) -> Unit,
    onSelectModel: (ModelDecodedDto) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Enter the current mileage (odometer reading) for your vehicle.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = uiState.mileageInput,
            onValueChange = onMileageChange,
            label = { Text("Current Mileage") },
            placeholder = { Text("e.g., 50000") },
            leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = "Mileage") }, // Added Icon
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
                    Text("Enter numbers only", color = MaterialTheme.colorScheme.onSurfaceVariant) // Muted helper
                }
            },
            enabled = !uiState.isLoading
        )

        // Optional final model selection dropdown
        AnimatedVisibility(visible = uiState.needsModelSelection) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Multiple models match. Please select the specific one:",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                DropdownSelection(
                    label = "Select Final Specific Model",
                    options = uiState.availableModels,
                    selectedOption = uiState.selectedModelId?.let { id -> uiState.availableModels.find { it.modelId == id } },
                    onOptionSelected = onSelectModel,
                    optionToString = { model ->
                        "${uiState.confirmedEngine?.engineType ?: ""} ${uiState.confirmedBody?.bodyType ?: ""} (${model.year}) (ID: ${model.modelId})"
                    },
                    isEnabled = !uiState.isLoading && uiState.availableModels.isNotEmpty(),
                    isError = uiState.needsModelSelection && uiState.selectedModelId == null,
                    errorText = if (uiState.needsModelSelection && uiState.selectedModelId == null) "Specific model required" else null
                )
            }
        }
    }
}
