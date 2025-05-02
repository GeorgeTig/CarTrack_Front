package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection // Import if moved

@Composable
internal fun SeriesStepContent(
    uiState: AddVehicleUiState,
    onSelectProducer: (String) -> Unit,
    onSelectSeries: (VinDecodedResponseDto) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Show Producer dropdown only if needed
        AnimatedVisibility(visible = uiState.needsProducerSelection) {
            Column {
                DropdownSelection(
                    label = "Select Make / Producer",
                    options = uiState.availableProducers,
                    selectedOption = uiState.selectedProducer,
                    onOptionSelected = onSelectProducer,
                    optionToString = { it },
                    isEnabled = !uiState.isLoading,
                    showRequiredMarker = uiState.selectedProducer == null
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Show Series dropdown (always shown in this step if producer selected/auto-selected)
        DropdownSelection(
            label = "Select Series",
            options = uiState.availableSeries,
            selectedOption = uiState.selectedSeriesDto,
            onOptionSelected = onSelectSeries,
            optionToString = { "${it.producer ?: "Unknown"} ${it.seriesName ?: "Unknown Series"}" },
            isEnabled = !uiState.isLoading && uiState.selectedProducer != null,
            showRequiredMarker = uiState.needsSeriesSelection && uiState.selectedSeriesDto == null
        )
    }
}