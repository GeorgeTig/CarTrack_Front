package com.example.cartrack.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Import ViewModel when created: import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    // viewModel: HomeViewModel = hiltViewModel(), // Inject ViewModel later
    onVehicleSelected: (Int) -> Unit,
) {
    // val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Observe state later

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Home Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Text("(Vehicle list will appear here)")
            Spacer(modifier = Modifier.height(16.dp))

            // TODO: Replace with actual vehicle list display and selection logic
            Button(onClick = { onVehicleSelected(123) }) { // Example action
                Text("Select Vehicle 123 (Test)")
            }
        }
    }
}