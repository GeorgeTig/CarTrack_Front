package com.example.cartrack.feature.home.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.cartrack.feature.home.presentation.helpers.HomeTopAppBar
import com.example.cartrack.feature.home.presentation.helpers.SelectedVehicleDetails
import com.example.cartrack.feature.home.presentation.helpers.VehicleSelectorRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.feature.auth.presentation.AuthViewModel
import com.example.cartrack.feature.home.presentation.helpers.cards.VehicleDetailInfoCard
import com.example.cartrack.feature.home.presentation.helpers.cards.VehicleHealthCard
import com.example.cartrack.feature.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    appNavController: NavHostController
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val hasNewNotifications by authViewModel.hasNewNotifications.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("HomeScreen", "Lifecycle ON_RESUME, refreshing vehicles.")
                homeViewModel.loadVehicles()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Scaffold(
        topBar = {
            HomeTopAppBar(
                hasNewNotifications = hasNewNotifications,
                onNotificationsClick = { appNavController.navigate(Routes.NOTIFICATIONS) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            homeUiState.isLoadingVehicleList && homeUiState.vehicles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            homeUiState.vehicleListError != null && homeUiState.vehicles.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error: ${homeUiState.vehicleListError}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
            }
            homeUiState.vehicles.isEmpty() -> { // Cazul când nu sunt vehicule
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No vehicles in your garage.", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            // Folosește funcția helper din Routes pentru a construi ruta
                            // fromLoginNoVehicles va fi false aici, deoarece suntem deja în HomeScreen
                            appNavController.navigate(Routes.addVehicleRoute(fromLoginNoVehicles = false))
                        }) {
                            Text("Add Your First Vehicle")
                        }
                    }
                }
            }
            else -> { // Cazul când există vehicule
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    VehicleSelectorRow(
                        vehicles = homeUiState.vehicles,
                        selectedVehicleId = homeUiState.selectedVehicle?.id,
                        onVehicleSelect = { vehicleId ->
                            homeViewModel.onVehicleSelected(vehicleId)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    homeUiState.selectedVehicle?.let { vehicle ->
                        SelectedVehicleContent(
                            vehicle = vehicle,
                            vehicleInfo = homeUiState.selectedVehicleInfo,
                            onNavigateToFullDetails = {
                                Log.d("HomeScreen", "Navigate to full details for vehicle ID: ${vehicle.id}")
                                // TODO: appNavController.navigate(Routes.carHistoryRoute(vehicle.id))
                                // Sau la un ecran de detalii mai complet
                            }
                        )
                    } ?: run { // Acest run e puțin probabil dacă vehicles nu e goală și selectedVehicle e primul by default
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Select a vehicle to see details.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedVehicleContent(
    vehicle: VehicleResponseDto,
    vehicleInfo: VehicleInfoResponseDto?,
    onNavigateToFullDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = vehicle.series,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        VehicleDetailInfoCard(
            vehicle = vehicle,
            vehicleInfo = vehicleInfo,
            onNavigateToFullDetails = onNavigateToFullDetails
        )
        Spacer(modifier = Modifier.height(16.dp))
        VehicleHealthCard()
    }
}