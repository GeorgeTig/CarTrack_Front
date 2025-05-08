package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection // Import your themed dropdown

@Composable
internal fun SeriesStepContent(
    uiState: AddVehicleUiState,
    onSelectProducer: (String) -> Unit,
    onSelectSeries: (VinDecodedResponseDto) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show Producer dropdown only if needed
        AnimatedVisibility(visible = uiState.needsProducerSelection) {
            DropdownSelection(
                label = "Select Make / Producer",
                options = uiState.availableProducers,
                selectedOption = uiState.selectedProducer,
                onOptionSelected = onSelectProducer,
                optionToString = { it },
                isEnabled = !uiState.isLoading,
                isError = uiState.needsProducerSelection && uiState.selectedProducer == null,
                errorText = if(uiState.needsProducerSelection && uiState.selectedProducer == null) "Producer required" else null
            )
        }

        // Show Series dropdown
        DropdownSelection(
            label = "Select Series",
            options = uiState.availableSeries,
            selectedOption = uiState.selectedSeriesDto,
            onOptionSelected = onSelectSeries,
            optionToString = { "${it.producer} ${it.seriesName}" },
           isEnabled = !uiState.isLoading && (!uiState.needsProducerSelection || uiState.selectedProducer != null),
            isError = uiState.needsSeriesSelection && uiState.selectedSeriesDto == null,
            errorText = if(uiState.needsSeriesSelection && uiState.selectedSeriesDto == null) "Series required" else null
        )
    }
}