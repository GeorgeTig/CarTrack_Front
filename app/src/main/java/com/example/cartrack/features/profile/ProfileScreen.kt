package com.example.cartrack.features.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    // --- LOGICA NOUĂ PENTRU REFRESH ---
    val resultRecipient = appNavController.currentBackStackEntry
    val shouldRefresh by resultRecipient
        ?.savedStateHandle
        ?.getStateFlow("should_refresh_profile", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh == true) {
            Log.d("ProfileScreen", "Refresh triggered from edit screen.")
            viewModel.loadProfileData()
            // Resetează flag-ul pentru a nu reîncărca la fiecare recompoziție
            resultRecipient?.savedStateHandle?.set("should_refresh_profile", false)
        }
    }

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
                Column(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfileData() }) { Text("Retry") }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(vertical = 24.dp)) {
                    item(key = "profile_header") {
                        uiState.userInfo?.let { user ->
                            ProfileHeader(
                                user = user,
                                garageCount = uiState.vehicles.size,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    item(key = "divider") { Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) }
                    item(key = "garage_header") {
                        Text("My Garage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(12.dp))
                    }
                    if (uiState.vehicles.isEmpty()) {
                        item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No vehicles found.") } }
                    } else {
                        items(uiState.vehicles, key = { it.id }) { vehicle ->
                            VehicleProfileCard(
                                vehicle = vehicle,
                                isSelected = vehicle.id == uiState.activeVehicleId,
                                onClick = { viewModel.setActiveVehicle(vehicle.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                    item(key = "add_button") {
                        OutlinedButton(
                            onClick = { appNavController.navigate(Routes.addVehicleRoute(fromLoginNoVehicles = false)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, "Add Vehicle")
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Add New Vehicle")
                        }
                    }
                }
            }
        }
    }
}