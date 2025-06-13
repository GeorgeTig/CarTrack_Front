package com.example.cartrack.features.add_maintenance

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceDateField
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceOptionalInfoSection
import com.example.cartrack.features.add_vehicle.components.DropdownSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    navController: NavHostController,
    viewModel: AddMaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.eventFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isFormEnabled = !uiState.isLoading && !uiState.isSaving

    LaunchedEffect(event) {
        event?.let {
            when (it) {
                is AddMaintenanceEvent.ShowToast -> Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                is AddMaintenanceEvent.NavigateBack -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh_history", true)
                    navController.popBackStack()
                }
            }
            viewModel.eventConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(uiState.currentVehicleSeries != "Vehicle") "Log for ${uiState.currentVehicleSeries}" else "Log New Maintenance") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(onClick = { viewModel.saveMaintenance() }) {
                    Icon(Icons.Filled.Save, "Save Maintenance Log")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchInitialData() }) { Text("Retry") }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text("Service Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MaintenanceDateField(
                                selectedDate = uiState.date,
                                onDateSelected = viewModel::onDateChange,
                                isEnabled = isFormEnabled
                            )
                            OutlinedTextField(
                                value = uiState.mileage,
                                onValueChange = viewModel::onMileageChange,
                                label = { Text("Mileage at time of service*") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                singleLine = true,
                                isError = uiState.mileageError != null,
                                supportingText = { if (uiState.mileageError != null) Text(uiState.mileageError!!, color = MaterialTheme.colorScheme.error) },
                                enabled = isFormEnabled
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Log Entries", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (uiState.logEntries.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                    Text("No entries added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                uiState.logEntries.forEach { entry ->
                                    key(entry.id) {
                                        when(entry) {
                                            is LogEntryItem.Scheduled -> ScheduledEntryCard(entry = entry, viewModel = viewModel, isEnabled = isFormEnabled)
                                            is LogEntryItem.Custom -> CustomEntryCard(entry = entry, viewModel = viewModel, isEnabled = isFormEnabled)
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = viewModel::addScheduledTask, modifier = Modifier.weight(1f), enabled = isFormEnabled) {
                                    Icon(Icons.Default.PlaylistAddCheck, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add Scheduled")
                                }
                                OutlinedButton(onClick = viewModel::addCustomTask, modifier = Modifier.weight(1f), enabled = isFormEnabled) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add Custom")
                                }
                            }
                            AnimatedVisibility(visible = uiState.entriesError != null) {
                                Text(uiState.entriesError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start=16.dp, top=4.dp))
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Divider(Modifier.padding(top = 8.dp, bottom = 24.dp))
                        Text("Optional Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        MaintenanceOptionalInfoSection(
                            serviceProvider = uiState.serviceProvider,
                            onServiceProviderChange = viewModel::onServiceProviderChange,
                            cost = uiState.cost,
                            onCostChange = viewModel::onCostChange,
                            notes = uiState.notes,
                            onNotesChange = viewModel::onNotesChange,
                            isEnabled = isFormEnabled
                        )
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledEntryCard(
    entry: LogEntryItem.Scheduled,
    viewModel: AddMaintenanceViewModel,
    isEnabled: Boolean // <-- Parametrul adăugat
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scheduled Task", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.removeLogEntry(entry.id) }, enabled = isEnabled) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DropdownSelection(
                    label = "Maintenance Type",
                    options = uiState.availableMaintenanceTypes,
                    selectedOption = uiState.availableMaintenanceTypes.find { it.id == entry.selectedTypeId },
                    onOptionSelected = { viewModel.onScheduledEntryUpdate(entry.id, it.id, null) },
                    optionToString = { it.name },
                    isEnabled = isEnabled // <-- Folosim parametrul
                )
                AnimatedVisibility(visible = entry.selectedTypeId != null) {
                    val usedIds = uiState.logEntries.mapNotNull { (it as? LogEntryItem.Scheduled)?.selectedReminderId }.toSet()
                    val availableTasks = uiState.availableScheduledTasks.filter { it.typeId == entry.selectedTypeId && (it.configId !in usedIds || it.configId == entry.selectedReminderId) }
                    DropdownSelection(
                        label = "Specific Task",
                        options = availableTasks,
                        selectedOption = availableTasks.find { it.configId == entry.selectedReminderId },
                        onOptionSelected = { viewModel.onScheduledEntryUpdate(entry.id, entry.selectedTypeId, it.configId) },
                        optionToString = { it.name },
                        isEnabled = isEnabled && availableTasks.isNotEmpty(), // <-- Folosim parametrul
                        placeholderText = if(availableTasks.isEmpty()) "No tasks for this type" else "Select..."
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomEntryCard(
    entry: LogEntryItem.Custom,
    viewModel: AddMaintenanceViewModel,
    isEnabled: Boolean // <-- Parametrul adăugat
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Custom Task", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.removeLogEntry(entry.id) }, enabled = isEnabled) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DropdownSelection(
                    label = "Maintenance Type",
                    options = uiState.availableMaintenanceTypes,
                    selectedOption = uiState.availableMaintenanceTypes.find { it.id == entry.selectedTypeId },
                    onOptionSelected = { viewModel.onCustomEntryUpdate(entry.id, it.id, entry.name) },
                    optionToString = { it.name },
                    isEnabled = isEnabled // <-- Folosim parametrul
                )
                AnimatedVisibility(visible = entry.selectedTypeId != null) {
                    OutlinedTextField(
                        value = entry.name,
                        onValueChange = { viewModel.onCustomEntryUpdate(entry.id, entry.selectedTypeId, it) },
                        label = { Text("Describe Task") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isEnabled // <-- Folosim parametrul
                    )
                }
            }
        }
    }
}