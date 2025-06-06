package com.example.cartrack.feature.addvehicle.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addvehicle.presentation.AddVehicleUiState
import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection

@Composable
internal fun SeriesYearStep(
    uiState: AddVehicleUiState,
    onSeriesAndYearSelected: (seriesName: String, year: Int) -> Unit,
    isLoading: Boolean
) {
    val seriesYearOptions = uiState.availableSeriesAndYears
    val currentSelectionPair = uiState.selectedSeriesName?.let { series -> uiState.selectedYear?.let { year -> series to year } }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Filled.TimeToLeave, contentDescription = "Series", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Vehicle Series & Year", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }

        if (uiState.allDecodedOptions.isEmpty() && !uiState.isLoadingVinDetails) { // Modul manual complet
            Text("Manual input fields for Producer, Series, Year - TBD", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)

        } else if (seriesYearOptions.isEmpty() && !uiState.isLoadingVinDetails) {
            Text("No specific series/year options found from VIN. Consider manual entry or check VIN.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        else {
            DropdownSelection(
                label = "Select Series & Year*",
                options = seriesYearOptions,
                selectedOption = currentSelectionPair,
                onOptionSelected = { pair -> onSeriesAndYearSelected(pair.first, pair.second) },
                optionToString = { "${it.first} (${it.second})" },
                isEnabled = !isLoading && seriesYearOptions.isNotEmpty(),
                isError = !isLoading && seriesYearOptions.isNotEmpty() && currentSelectionPair == null && (uiState.selectedSeriesName != null || uiState.selectedYear != null) ,
                errorText = if(!isLoading && seriesYearOptions.isNotEmpty() && currentSelectionPair == null && (uiState.selectedSeriesName != null || uiState.selectedYear != null)) "Selection required" else null,
                placeholderText = if (isLoading) "Loading..." else "Select Series & Year"
            )
        }
    }
}