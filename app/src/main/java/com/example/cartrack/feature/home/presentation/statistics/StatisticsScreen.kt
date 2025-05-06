package com.example.cartrack.feature.home.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(
                    "Error loading statistics: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            uiState.selectedVehicleId == null -> {
                Text("Please select a vehicle to view statistics.")
            }
            else -> {
                // TODO: Display actual statistics UI using uiState.statsData
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Car Statistics", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Statistics for Vehicle ID: ${uiState.selectedVehicleId}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Placeholder Data: ${uiState.statsData}") // Replace with actual charts/data
                }
            }
        }
    }
}