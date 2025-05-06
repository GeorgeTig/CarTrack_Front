package com.example.cartrack.feature.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications // Import Notification Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // For Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.core.ui.cards.VehicleInfoCard // Ensure this path is correct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current // For Toast message

    val rotationAngle by animateFloatAsState(
        targetValue = if (uiState.isDropdownExpanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    Box(modifier = Modifier.fillMaxSize()) { // Main container Box

        // --- Top Area: Vehicle Selector (Left) and Notification Icon (Right) ---
        // We can use a Row to position items at the start and end of the top area,
        // but the centering of "Your Vehicle" text above a left-aligned dropdown is tricky with a single Row.
        // Let's keep the Column for the selector and add the Icon separately to TopEnd.

        // Column for "Your vehicle" text and the Dropdown (aligned to TopStart of the Box)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart) // This Column is at the top-start
                .fillMaxWidth() // Takes full width for text centering
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Your vehicle",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )

            // Conditional display for the dropdown or its placeholder
            when {
                !uiState.isLoading && uiState.selectedVehicle != null -> {
                    ExposedDropdownMenuBox(
                        expanded = uiState.isDropdownExpanded,
                        onExpandedChange = { viewModel.toggleDropdown() },
                        modifier = Modifier.align(Alignment.Start) // Dropdown itself aligned left
                    ) {
                        Box(
                            modifier = Modifier
                                .menuAnchor()
                                .clickable { viewModel.toggleDropdown(true) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VehicleInfoCard(vehicle = uiState.selectedVehicle)
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Select vehicle",
                                    modifier = Modifier.padding(start = 8.dp).rotate(rotationAngle),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = uiState.isDropdownExpanded,
                            onDismissRequest = { viewModel.toggleDropdown(false) },
                            modifier = Modifier.background(Color.Transparent)
                        ) {
                            if (uiState.dropdownVehicles.isEmpty() && uiState.vehicles.size <= 1) {
                                DropdownMenuItem( /* ... No other vehicles ... */
                                    text = { Text("No other vehicles to select") },
                                    onClick = { viewModel.toggleDropdown(false) },
                                    enabled = false,
                                    colors = MenuDefaults.itemColors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                )
                            } else {
                                uiState.dropdownVehicles.forEach { vehicleInList ->
                                    DropdownMenuItem( /* ... VehicleInfoCard item ... */
                                        text = { VehicleInfoCard(vehicle = vehicleInList) },
                                        onClick = { viewModel.onVehicleSelected(vehicleInList.id) },
                                        contentPadding = PaddingValues(0.dp),
                                     )
                                }
                            }
                        }
                    }
                }
                uiState.isLoading && uiState.selectedVehicle == null -> {
                    VehicleInfoCard(vehicle = null, modifier = Modifier.align(Alignment.Start))
                }
                !uiState.isLoading && uiState.selectedVehicle == null && uiState.error == null -> {
                    VehicleInfoCard(vehicle = null, modifier = Modifier.align(Alignment.Start))
                    Text("No vehicle available.", modifier = Modifier.align(Alignment.Start).padding(top = 4.dp))
                }
            }
        }

        // --- Notification Icon Button (Top Right) ---
        IconButton(
            onClick = {
                // TODO: Handle notification click - For now, show a Toast
                android.widget.Toast.makeText(context, "Notifications clicked!", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .align(Alignment.TopEnd) // Align to the top-right of the Box
                .padding(top = 8.dp, end = 16.dp) // Padding for the icon
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Or primary
            )
        }


        // --- Rest of your HomeScreen content (Main details area) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Adjust top padding to clear BOTH the dropdown selector AND the notification icon if they overlap.
                // This might require more padding than before.
                .padding(top = 170.dp) // TUNE THIS VALUE CAREFULLY
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // This content remains the same
            if (uiState.isLoading && uiState.selectedVehicle == null) {
                CircularProgressIndicator()
                Text("Loading garage details...")
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.refreshData() }) { Text("Retry") }
            } else if (uiState.selectedVehicle != null) {
                Text(
                    "Detailed Information Area",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("Content for vehicle ID: ${uiState.selectedVehicle!!.id} would go here.")
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Full Details for ${uiState.selectedVehicle!!.series}", style = MaterialTheme.typography.titleMedium)
                        Text("VIN: ${uiState.selectedVehicle!!.vin}")
                        Text("Year: ${uiState.selectedVehicle!!.year.toString()}")
                    }
                }
            } else if (!uiState.isLoading) {
                Text("Please add a vehicle to see details.")
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { viewModel.refreshData() }, enabled = !uiState.isLoading) {
                if (uiState.isLoading && uiState.selectedVehicle != null) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Refresh Garage")
            }
        }
    }
}