package com.example.cartrack.feature.vehicle.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// Example icon
import androidx.compose.material.icons.filled.Info // Example icon (or KeyboardArrowRight)
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

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and Card onClick
@Composable
fun VehicleSelectionScreen(
    // Inject ViewModel for this screen
    viewModel: VehicleSelectionViewModel = hiltViewModel(),
    // --- Lambdas for Navigation Actions ---
    onVehicleSelected: (vehicleId: Int) -> Unit, // Called when a vehicle card is clicked
    onAddVehicleClicked: () -> Unit, // Called when the '+' FAB is clicked <<-- This one
    onLogout: () -> Unit // Called when logout action is triggered
    // --- End Lambdas ---
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Effect to show error Snackbar
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
                    // Logout Button in TopAppBar
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Optional styling
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // --- Floating Action Button ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVehicleClicked // <<-- Calls the lambda passed from Navigation
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Vehicle")
            }
        }
        // --- End Floating Action Button ---
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply Scaffold padding
        ) {
            when {
                // Loading State
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Empty State (No vehicles loaded, no error)
                !uiState.isLoading && uiState.vehicles.isEmpty() && uiState.error == null -> {
                    Text(
                        text = "You haven't added any vehicles yet.\nTap '+' to add one!",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Vehicle List Display
                uiState.vehicles.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp), // Padding around the list
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between cards
                    ) {
                        items(uiState.vehicles, key = { it.id }) { vehicle ->
                            VehicleItemCard(
                                vehicle = vehicle,
                                // Pass vehicle ID to the onVehicleSelected lambda when card is clicked
                                onClick = { onVehicleSelected(vehicle.id) }
                            )
                        }
                    }
                }
                // Error state is handled by the Snackbar launched effect outside the Box content
            }
        }
    }
}

// Card Composable for displaying a single vehicle (no changes needed here)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleItemCard(
    vehicle: VehicleResponseDto,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Subtle shadow
        shape = MaterialTheme.shapes.medium // Rounded corners
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.modelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VIN: ${vehicle.vin}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mileage: ${vehicle.mileage} km", // Adjust units if needed
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Info, // Indicate clickability
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}