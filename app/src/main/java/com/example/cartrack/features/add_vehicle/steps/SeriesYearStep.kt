package com.example.cartrack.features.add_vehicle.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.features.add_vehicle.AddVehicleUiState
import com.example.cartrack.features.add_vehicle.components.DropdownSelection

@Composable
fun SeriesYearStep(
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

        DropdownSelection(
            label = "Select Series & Year*",
            options = seriesYearOptions,
            selectedOption = currentSelectionPair,
            onOptionSelected = { pair -> onSeriesAndYearSelected(pair.first, pair.second) },
            optionToString = { "${it.first} (${it.second})" },
            isEnabled = !isLoading && seriesYearOptions.isNotEmpty(),
            isError = uiState.hasAttemptedNext && currentSelectionPair == null,
            errorText = "Selection is required",
            placeholderText = if (isLoading) "Loading..." else "Select from available options"
        )
    }
}