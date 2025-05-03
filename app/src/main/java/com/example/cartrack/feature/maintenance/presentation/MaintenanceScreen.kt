package com.example.cartrack.feature.maintenance.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Import ViewModel when created

@Composable
fun MaintenanceScreen(
    // viewModel: MaintenanceViewModel = hiltViewModel(), // Inject later
    // onLogout: () -> Unit // Pass logout action if needed here
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Maintenance Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Text("(Maintenance logs and actions will appear here)")
            // Example logout button - move to Profile ideally
            // Button(onClick = onLogout) { Text("Log Out (Test)") }
        }
    }
}