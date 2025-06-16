package com.example.cartrack.features.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.ConfirmationDialog
import com.example.cartrack.core.ui.components.EmptyState
import com.example.cartrack.features.profile.components.ProfileScreenShimmer
import com.example.cartrack.features.profile.helpers.ProfileHeader
import com.example.cartrack.features.profile.helpers.VehicleProfileCard
import com.example.cartrack.navigation.Routes
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appNavController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    val context = LocalContext.current

    val resultRecipient = appNavController.currentBackStackEntry
    val shouldRefresh by resultRecipient
        ?.savedStateHandle
        ?.getStateFlow("should_refresh_profile", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh == true) {
            Log.d("ProfileScreen", "Refresh triggered from a child screen.")
            viewModel.loadProfileData()
            resultRecipient?.savedStateHandle?.set("should_refresh_profile", false)
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null && !uiState.isLoading) {
            Toast.makeText(context, "Error: ${uiState.error}", Toast.LENGTH_LONG).show()
        }
    }

    if (uiState.vehicleToDelete != null) {
        ConfirmationDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            onConfirmation = viewModel::confirmDeleteVehicle,
            dialogTitle = "Delete Vehicle?",
            dialogText = "The vehicle will be marked as inactive and will no longer appear in your list. Are you sure?",
            icon = Icons.Default.Delete,
            isLoading = uiState.isDeleteLoading
        )
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
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.loadProfileData() },
            modifier = Modifier.padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.userInfo == null -> {
                    ProfileScreenShimmer()
                }
                uiState.error != null && uiState.userInfo == null -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text("An Error Occurred", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                                    Text(uiState.error!!, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                    Button(onClick = { viewModel.loadProfileData() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 24.dp)
                    ) {
                        item(key = "profile_header") {
                            uiState.userInfo?.let { user ->
                                ProfileHeader(
                                    user = user,
                                    garageCount = uiState.vehicles.size,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        item(key = "divider") {
                            Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp))
                        }

                        item(key = "garage_header") {
                            Text(
                                "My Garage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        if (uiState.vehicles.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.DirectionsCar,
                                    title = "Garage is Empty",
                                    subtitle = "Add your first vehicle to start tracking it.",
                                    modifier = Modifier.padding(top = 32.dp)
                                )
                            }
                        } else {
                            items(uiState.vehicles, key = { it.id }) { vehicle ->
                                VehicleProfileCard(
                                    vehicle = vehicle,
                                    onCardClick = {
                                        appNavController.navigate(Routes.carHistoryRoute(vehicle.id))
                                    },
                                    onDeleteClick = {
                                        viewModel.onShowDeleteDialog(vehicle.id)
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }

                        item(key = "add_button") {
                            OutlinedButton(
                                onClick = { appNavController.navigate(Routes.addVehicleRoute(fromLoginNoVehicles = false)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .padding(top = if (uiState.vehicles.isEmpty()) 16.dp else 0.dp)
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
}