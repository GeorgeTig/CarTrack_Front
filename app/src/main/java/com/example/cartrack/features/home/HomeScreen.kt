package com.example.cartrack.features.home

import android.Manifest
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import com.example.cartrack.features.home.helpers.*
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
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
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
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) { Text(uiState.error!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
            uiState.vehicles.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No vehicles in your garage.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { appNavController.navigate(Routes.addVehicleRoute(false)) }) { Text("Add Your First Vehicle") }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(8.dp))
                    VehicleSelectorRow(
                        vehicles = uiState.vehicles,
                        selectedVehicleId = uiState.selectedVehicle?.id,
                        onVehicleSelect = homeViewModel::onVehicleSelected
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    uiState.selectedVehicle?.let { vehicle ->
                        Text(
                            text = vehicle.series,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickActionsCard(
                                onLogMaintenance = { appNavController.navigate(Routes.ADD_MAINTENANCE) },
                                onViewHistory = { appNavController.navigate(Routes.carHistoryRoute(vehicle.id)) },
                                onSyncMileage = { /* TODO: Deschide un dialog pentru sync */ }
                            )

                            AnimatedContent(targetState = uiState.isLoadingDetails, label = "detailsLoading") { isLoading ->
                                if (isLoading) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large))
                                        Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large))
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        VehicleStatusCard(
                                            warnings = uiState.warnings,
                                            isExpanded = uiState.isWarningsExpanded,
                                            onToggleExpansion = homeViewModel::onToggleWarningsExpansion,
                                            onWarningClick = { reminderId -> appNavController.navigate(Routes.reminderDetailRoute(reminderId)) }
                                        )
                                        UsageChartCard(dailyUsage = uiState.dailyUsage)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}