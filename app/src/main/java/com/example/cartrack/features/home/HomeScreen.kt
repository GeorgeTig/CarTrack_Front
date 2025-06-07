package com.example.cartrack.features.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.features.home.helpers.HomeTopAppBar
import com.example.cartrack.features.home.helpers.VehicleHealthCard
import com.example.cartrack.features.home.helpers.VehicleInfoCard
import com.example.cartrack.features.home.helpers.VehicleSelectorRow
import com.example.cartrack.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appNavController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val hasNewNotifications by authViewModel.hasNewNotifications.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        lifecycle.currentStateFlow.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect {
                if(it == Lifecycle.State.RESUMED) {
                    Log.d("HomeScreen", "Resumed. Forcing refresh.")
                    homeViewModel.loadVehicles(forceRefresh = true)
                }
            }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                hasNewNotifications = hasNewNotifications,
                onNotificationsClick = { appNavController.navigate(Routes.NOTIFICATIONS) }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.vehicles.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(
                        "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            uiState.vehicles.isEmpty() && !uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No vehicles in your garage.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { appNavController.navigate(Routes.addVehicleRoute(false)) }) {
                            Text("Add Your First Vehicle")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
                ) {
                    VehicleSelectorRow(
                        vehicles = uiState.vehicles,
                        selectedVehicleId = uiState.selectedVehicle?.id,
                        onVehicleSelect = homeViewModel::onVehicleSelected
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    uiState.selectedVehicle?.let { vehicle ->
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = vehicle.series,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            VehicleInfoCard(vehicle = vehicle, vehicleInfo = uiState.selectedVehicleInfo)
                            VehicleHealthCard()
                            // Add more cards or actions here
                        }
                    }
                }
            }
        }
    }
}