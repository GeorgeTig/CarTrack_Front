package com.example.cartrack.feature.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.feature.navigation.Routes
import com.example.cartrack.feature.profile.presentation.helpers.ProfileHeader
import com.example.cartrack.feature.profile.presentation.helpers.VehicleProfileCard

/**
 * Ecranul principal de Profil al utilizatorului.
 * Afișează informațiile utilizatorului, statisticile, o listă verticală de vehicule
 * și oferă navigare către setări.
 *
 * @param viewModel ViewModel-ul care gestionează logica acestui ecran.
 * @param onLogout Callback pentru a declanșa acțiunea de logout la nivel de aplicație.
 * @param appNavController Controller-ul de navigare global.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    appNavController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = {
                        appNavController.navigate(Routes.SETTINGS)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfileData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Retry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    // 1. Antetul (Imagine, Nume, Statistici)
                    item(key = "profile_header") {
                        uiState.userInfo?.let { user ->
                            ProfileHeader(
                                user = user,
                                garageCount = uiState.vehicles.size,
                                maintenanceLogsCount = 0, // Placeholder
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // 2. Divider-ul de separare
                    item(key = "header_divider") {
                        Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp))
                    }

                    // 3. Titlul pentru secțiunea "My Garage" sau mesajul "No vehicles"
                    item(key = "garage_section_header") {
                        if (uiState.vehicles.isNotEmpty()) {
                            Text(
                                "My Garage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No vehicles in your garage yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4. Lista verticală de vehicule
                    items(uiState.vehicles, key = { it.id }) { vehicle ->
                        VehicleProfileCard(
                            vehicle = vehicle,
                            isSelected = vehicle.id == uiState.activeVehicleId,
                            onClick = { viewModel.setActiveVehicle(vehicle.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 5. Butonul de adăugare vehicul, la finalul listei
                    item(key = "add_vehicle_button") {
                        // Adăugăm spațiu deasupra pentru a nu fi lipit de ultimul card/mesaj
                        val topPadding = if (uiState.vehicles.isNotEmpty()) 12.dp else 0.dp

                        OutlinedButton(
                            onClick = { appNavController.navigate(Routes.addVehicleRoute()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = topPadding)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Add New Vehicle")
                        }
                    }
                }
            }
        }
    }
}