package com.example.cartrack.feature.home.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.core.ui.cards.VehicleInfoCard
import com.example.cartrack.feature.home.presentation.details.DetailsScreen
import com.example.cartrack.feature.home.presentation.statistics.StatisticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val rotationAngle by animateFloatAsState(
        targetValue = if (homeUiState.isDropdownExpanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    val tabs = listOf(HomeTab.STATISTICS, HomeTab.DETAILS)

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Top Section: Selector Row ---
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
                    // Explicitly using a theme color for the label
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
                            // Default text color from theme
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } // End Dropdown Column

            IconButton(
                onClick = { android.widget.Toast.makeText(context, "Notifications!", android.widget.Toast.LENGTH_SHORT).show() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    // Explicit theme color for icon tint
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } // End Top Row

        // --- Tab Row ---
        TabRow(selectedTabIndex = tabs.indexOf(homeUiState.selectedTab)) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = homeUiState.selectedTab == tab,
                    onClick = { homeViewModel.selectTab(tab) },
                    text = { Text(text = tab.name.replaceFirstChar { it.titlecase() }) } // Text color adapts
                )
            }
        }

        // --- Content Area based on Selected Tab ---
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
            // Loading indicator - uses theme default color (primary)
            if (homeUiState.isLoadingVehicleList && homeUiState.selectedVehicle == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Error text - explicitly uses theme error color
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