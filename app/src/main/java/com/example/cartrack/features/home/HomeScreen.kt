package com.example.cartrack.features.home

import android.widget.Toast
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
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.EmptyState
import com.example.cartrack.core.ui.components.HomeDetailsShimmer
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.features.home.helpers.HomeTopAppBar
import com.example.cartrack.features.home.helpers.QuickActionsCard
import com.example.cartrack.features.home.helpers.TitleHeader
import com.example.cartrack.features.home.helpers.UsageChartCard
import com.example.cartrack.features.home.helpers.VehicleInfoCard
import com.example.cartrack.features.home.helpers.VehicleSelectorRow
import com.example.cartrack.features.home.helpers.VehicleStatusCard
import com.example.cartrack.navigation.Routes
// import com.google.accompanist.permissions.* // <-- ȘTERGE ACESTE IMPORTURI
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appNavController: NavHostController,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    val hasNewNotifications by authViewModel.hasNewNotifications.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Am eliminat logica de permisiuni de aici

    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Logica de refresh rămâne, dar fără verificarea permisiunilor
            homeViewModel.loadVehicles(forceRefresh = true)
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.eventFlow.collect { event ->
            when (event) {
                is HomeEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Am eliminat dialogul de permisiuni de aici

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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Am eliminat `LocationWeatherRow` și logica de permisiuni de aici.
                                VehicleSelectorRow(
                                    vehicles = uiState.vehicles,
                                    selectedVehicleId = uiState.selectedVehicle?.id,
                                    onVehicleSelect = { vehicleId ->
                                        homeViewModel.onVehicleSelected(vehicleId, uiState.vehicles)
                                    }
                                )
                                Divider()
                            }
                        }

                        uiState.selectedVehicle?.let { vehicle ->
                            item(key = "vehicle_header_${vehicle.id}") {
                                TitleHeader(
                                    vehicle = vehicle,
                                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                                )
                            }

                            item(key = "vehicle_content_${vehicle.id}") {
                                AnimatedContent(
                                    targetState = uiState.isLoadingDetails,
                                    label = "detailsLoading",
                                    modifier = Modifier.padding(top = 16.dp)
                                ) { isLoading ->
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        if (isLoading) {
                                            HomeDetailsShimmer()
                                        } else {
                                            VehicleInfoCard(
                                                vehicle = vehicle,
                                                vehicleInfo = uiState.selectedVehicleInfo,
                                                onViewDetailsClick = {
                                                    // Aici poți naviga la un ecran de detalii complete, dacă îl creezi
                                                }
                                            )

                                            VehicleStatusCard(
                                                warnings = uiState.warnings,
                                                isExpanded = uiState.isWarningsExpanded,
                                                onToggleExpansion = homeViewModel::onToggleWarningsExpansion,
                                                onWarningClick = { reminderId -> appNavController.navigate(Routes.reminderDetailRoute(reminderId)) }
                                            )

                                            QuickActionsCard(
                                                onLogServiceClick = { appNavController.navigate(Routes.ADD_MAINTENANCE) },
                                                onViewHistoryClick = { appNavController.navigate(Routes.carHistoryRoute(vehicle.id)) },
                                                onSyncMileageClick = { homeViewModel.showSyncMileageDialog() }
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