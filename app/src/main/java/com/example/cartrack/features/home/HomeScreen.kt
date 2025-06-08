package com.example.cartrack.features.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoCrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.EmptyState
import com.example.cartrack.core.ui.components.HomeDetailsShimmer
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.features.home.helpers.*
import com.example.cartrack.navigation.Routes
import com.google.accompanist.permissions.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    appNavController: NavHostController,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    val hasNewNotifications by authViewModel.hasNewNotifications.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val context = LocalContext.current

    var showPermissionRationale by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            Log.d("HomeScreen", "Permission granted after request.")
            homeViewModel.fetchLocationAndWeather()
        } else {
            Log.d("HomeScreen", "Permission denied after request.")
        }
    }

    LaunchedEffect(lifecycle) {
        lifecycle.currentStateFlow.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    Log.d("HomeScreen", "Resumed. Forcing data refresh.")
                    homeViewModel.loadVehicles(forceRefresh = true)

                    if (locationPermissionState.status.isGranted) {
                        homeViewModel.fetchLocationAndWeather()
                    }
                }
            }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Location Permission Required") },
            text = { Text("To show local weather, this app needs access to your device's location. Your location is not stored or shared.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionRationale = false
                    locationPermissionState.launchPermissionRequest()
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                hasNewNotifications = hasNewNotifications,
                onNotificationsClick = { appNavController.navigate(Routes.NOTIFICATIONS) }
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { homeViewModel.loadVehicles(forceRefresh = true) },
            modifier = Modifier.padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.vehicles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("An Error Occurred", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                                    Text(uiState.error!!, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                uiState.vehicles.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.NoCrash,
                        title = "Welcome to Your Garage!",
                        subtitle = "You haven't added any vehicles yet. Let's add your first one to get started.",
                        actionText = "Add Your First Vehicle",
                        onActionClick = { appNavController.navigate(Routes.addVehicleRoute(false)) }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            val permissionStatus = locationPermissionState.status
                            when {
                                permissionStatus.isGranted -> {
                                    LocationWeatherRow(
                                        locationData = uiState.locationData,
                                        lastSync = uiState.lastSyncTime
                                    )
                                }
                                permissionStatus.shouldShowRationale -> {
                                    PermissionRationaleView { showPermissionRationale = true }
                                }
                                else -> {
                                    PermissionPermanentlyDeniedView {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = Uri.fromParts("package", context.packageName, null)
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }
                        item {
                            VehicleSelectorRow(
                                vehicles = uiState.vehicles,
                                selectedVehicleId = uiState.selectedVehicle?.id,
                                onVehicleSelect = homeViewModel::onVehicleSelected
                            )
                        }
                        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

                        uiState.selectedVehicle?.let { vehicle ->
                            item(key = "vehicle_header_${vehicle.id}") {
                                Text(
                                    text = vehicle.series,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                            item(key = "vehicle_content_${vehicle.id}") {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    QuickActionsCard(
                                        onLogMaintenance = { appNavController.navigate(Routes.ADD_MAINTENANCE) },
                                        onViewHistory = { appNavController.navigate(Routes.carHistoryRoute(vehicle.id)) },
                                        onSyncMileage = { homeViewModel.showSyncMileageDialog() }
                                    )

                                    AnimatedContent(targetState = uiState.isLoadingDetails, label = "detailsLoading") { isLoading ->
                                        if (isLoading) {
                                            HomeDetailsShimmer()
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
    }
}