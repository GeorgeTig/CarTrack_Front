package com.example.cartrack.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.profile.helpers.ProfileHeader
import com.example.cartrack.features.profile.helpers.VehicleProfileCard
import com.example.cartrack.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appNavController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { appNavController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::loadProfileData) {
                        Icon(Icons.Filled.Refresh, "Retry"); Text("Retry")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    item {
                        uiState.userInfo?.let { user ->
                            ProfileHeader(
                                user = user,
                                garageCount = uiState.vehicles.size,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    item { Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) }
                    item {
                        Text(
                            "My Garage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    items(uiState.vehicles, key = { it.id }) { vehicle ->
                        VehicleProfileCard(
                            vehicle = vehicle,
                            isSelected = vehicle.id == uiState.activeVehicleId,
                            onClick = { viewModel.setActiveVehicle(vehicle.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { appNavController.navigate(Routes.addVehicleRoute(false)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, "Add Vehicle"); Text("Add New Vehicle")
                        }
                    }
                }
            }
        }
    }
}