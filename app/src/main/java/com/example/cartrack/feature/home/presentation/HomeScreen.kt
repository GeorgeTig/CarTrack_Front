package com.example.cartrack.feature.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.* // Import Material3 BadgedBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.cards.VehicleInfoCard
import com.example.cartrack.feature.auth.presentation.AuthViewModel // Import AuthViewModel
import com.example.cartrack.feature.home.presentation.details.DetailsScreen
import com.example.cartrack.feature.home.presentation.statistics.StatisticsScreen
import com.example.cartrack.feature.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class) // Opt-in pentru BadgedBox
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(), // Get AuthViewModel for notification status
    appNavController: NavHostController
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val hasNewNotifications by authViewModel.hasNewNotifications.collectAsStateWithLifecycle() // Collect status

    val rotationAngle by animateFloatAsState(
        targetValue = if (homeUiState.isDropdownExpanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    val tabs = listOf(HomeTab.STATISTICS, HomeTab.DETAILS)

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your vehicle",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    textAlign = TextAlign.Center
                )
                when {
                    !homeUiState.isLoadingVehicleList && homeUiState.selectedVehicle != null -> {
                        ExposedDropdownMenuBox(
                            expanded = homeUiState.isDropdownExpanded,
                            onExpandedChange = { homeViewModel.toggleDropdown() },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Box(modifier = Modifier.menuAnchor().clickable { homeViewModel.toggleDropdown(true) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    VehicleInfoCard(vehicle = homeUiState.selectedVehicle)
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Select vehicle",
                                        modifier = Modifier.padding(start = 4.dp).rotate(rotationAngle),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = homeUiState.isDropdownExpanded,
                                onDismissRequest = { homeViewModel.toggleDropdown(false) },
                                modifier = Modifier.background(Color.Transparent)
                            ) {
                                if (homeUiState.dropdownVehicles.isEmpty() && homeUiState.vehicles.size <= 1) {
                                    DropdownMenuItem(text = { Text("No other vehicles") }, onClick = { homeViewModel.toggleDropdown(false) }, enabled = false,
                                        colors = MenuDefaults.itemColors(disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                                    )
                                } else {
                                    homeUiState.dropdownVehicles.forEach { vehicleInList ->
                                        DropdownMenuItem(
                                            text = { VehicleInfoCard(vehicle = vehicleInList) },
                                            onClick = { homeViewModel.onVehicleSelected(vehicleInList.id) },
                                            contentPadding = PaddingValues(0.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    homeUiState.isLoadingVehicleList -> {
                        VehicleInfoCard(vehicle = null, modifier = Modifier.align(Alignment.Start))
                    }
                    else -> {
                        VehicleInfoCard(vehicle = null, modifier = Modifier.align(Alignment.Start))
                        Text(
                            "No vehicle.",
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Notifications button with Badge
            IconButton(
                onClick = { appNavController.navigate(Routes.NOTIFICATIONS) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (hasNewNotifications) {
                            Badge { /* Poți pune un Text aici dacă vrei număr, ex: Text("1") */ }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        TabRow(selectedTabIndex = tabs.indexOf(homeUiState.selectedTab)) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = homeUiState.selectedTab == tab,
                    onClick = { homeViewModel.selectTab(tab) },
                    text = { Text(text = tab.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            when (homeUiState.selectedTab) {
                HomeTab.DETAILS -> {
                    DetailsScreen()
                }
                HomeTab.STATISTICS -> {
                    StatisticsScreen()
                }
            }
            if (homeUiState.isLoadingVehicleList && homeUiState.selectedVehicle == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (homeUiState.vehicleListError != null && homeUiState.selectedVehicle == null) {
                Text(
                    "Error: ${homeUiState.vehicleListError}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}