package com.example.cartrack.features.maintenance

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.services.getIconForMaintenanceType
import com.example.cartrack.core.ui.components.TypeFilterChip
import com.example.cartrack.features.reminders.ReminderItemCard
import com.example.cartrack.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    appNavController: NavHostController,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle, viewModel) {
        lifecycle.currentStateFlow.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .filter { it == Lifecycle.State.RESUMED }
            .collect {
                uiState.selectedVehicleId?.let { vehicleId ->
                    Log.d("MaintenanceScreen", "Resumed, fetching reminders for vehicle $vehicleId")
                    viewModel.fetchRemindersForVehicle(vehicleId, isRetry = true)
                }
            }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Reminders") },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(28.dp)
            )
        }

        // Main Tabs
        val mainTabs = MaintenanceMainTab.entries.toList()
        TabRow(selectedTabIndex = mainTabs.indexOf(uiState.selectedMainTab)) {
            mainTabs.forEach { tab ->
                Tab(
                    selected = uiState.selectedMainTab == tab,
                    onClick = { focusManager.clearFocus(); viewModel.selectMainTab(tab) },
                    text = { Text(tab.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        // Type Filter Chips
        AnimatedVisibility(visible = uiState.availableTypes.isNotEmpty() || uiState.isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TypeFilterChip(
                        chipData = com.example.cartrack.core.ui.components.FilterChipData(-1, "All Types", getIconForMaintenanceType(-1)),
                        isSelected = uiState.selectedTypeId == null,
                        onClick = { focusManager.clearFocus(); viewModel.selectTypeFilter(null) }
                    )
                }
                items(uiState.availableTypes, key = { it.id }) { typeItem ->
                    TypeFilterChip(
                        chipData = typeItem,
                        isSelected = uiState.selectedTypeId == typeItem.id,
                        onClick = { focusManager.clearFocus(); viewModel.selectTypeFilter(typeItem.id) }
                    )
                }
            }
        }

        // Content Area
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
                uiState.selectedVehicleId == null -> Text("Please select a vehicle from the Home screen.", modifier = Modifier.align(Alignment.Center))
                uiState.filteredReminders.isEmpty() -> Text("No reminders found for the current filters.", modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                    ) {
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onClick = { appNavController.navigate(Routes.reminderDetailRoute(reminder.configId)) }
                            )
                        }
                    }
                }
            }
        }
    }
}