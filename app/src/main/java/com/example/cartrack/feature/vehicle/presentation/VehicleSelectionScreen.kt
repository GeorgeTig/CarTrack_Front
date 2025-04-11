package com.example.cartrack.feature.vehicle.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.DirectionsCar // Example icon
import androidx.compose.material.icons.filled.Info // Example icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun VehicleSelectionScreen(
    viewModel: VehicleSelectionViewModel = hiltViewModel(),
    onVehicleSelected: (vehicleId: Int) -> Unit, // Callback when a vehicle is clicked
    onAddVehicleClicked: () -> Unit, // Callback for the '+' button
    onLogout: () -> Unit // Callback to trigger logout
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.errorShown() // Notify ViewModel that error has been shown
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Select Your Vehicle") },
                actions = {
                    TextButton(onClick = onLogout) { // Add logout button
                        Text("Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVehicleClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding
        ) {
            when {
                // --- Loading State ---
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // --- Empty State ---
                !uiState.isLoading && uiState.vehicles.isEmpty() && uiState.error == null -> {
                    Text(
                        text = "You haven't added any vehicles yet.\nTap '+' to add one!",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // --- Vehicle List ---
                uiState.vehicles.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between items
                    ) {
                        items(uiState.vehicles, key = { it.id }) { vehicle ->
                            VehicleItemCard(
                                vehicle = vehicle,
                                onClick = { onVehicleSelected(vehicle.id) }
                            )
                        }
                    }
                }
                // Error state is handled by the Snackbar launched effect
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For Card onClick
@Composable
private fun VehicleItemCard(
    vehicle: VehicleResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Add shadow
        shape = MaterialTheme.shapes.medium // Rounded corners
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon (Optional)
            // Icon(
            //      imageVector = Icons.Default.DirectionsCar,
            //        contentDescription = "Vehicle Icon",
            //       modifier = Modifier.size(40.dp),
            //        tint = MaterialTheme.colorScheme.primary
            //   )

            Spacer(modifier = Modifier.width(16.dp))

            // Vehicle Details Column
            Column(modifier = Modifier.weight(1f)) { // Take remaining space
                Text(
                    text = vehicle.modelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VIN: ${vehicle.vin}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Slightly muted color
                )
                Text(
                    text = "Mileage: ${vehicle.mileage} km", // Add units if appropriate
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Optional: Add a chevron or indicator for click action
            Icon(
                imageVector = Icons.Default.Info, // Or Icons.AutoMirrored.Filled.KeyboardArrowRight
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}