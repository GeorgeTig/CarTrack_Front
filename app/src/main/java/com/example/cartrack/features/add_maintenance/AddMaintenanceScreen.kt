package com.example.cartrack.features.add_maintenance

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto
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
    val context = LocalContext.current
    val isFormEnabled = !uiState.isLoading && !uiState.isSaving

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddMaintenanceEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is AddMaintenanceEvent.NavigateBackOnSuccess -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("should_refresh_history", true)
                    navController.popBackStack()
                }
            }
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
                FloatingActionButton(
                    onClick = { if (isFormEnabled) viewModel.saveMaintenance() }
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    } else {
                        Icon(Icons.Filled.Save, "Save Maintenance Log")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null && uiState.currentVehicleId == null -> {
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
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Log Maintenance",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Add Maintenance Log",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            if (uiState.currentVehicleSeries != "Vehicle") {
                                Text(
                                    text = uiState.currentVehicleSeries,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Divider()
                        Spacer(Modifier.height(24.dp))
                        Text("Service Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            MaintenanceDateField(
                                selectedDate = uiState.date,
                                onDateSelected = viewModel::onDateChange,
                                isEnabled = isFormEnabled
                            )
                            uiState.currentVehicleMileage?.let {
                                Text(
                                    text = "Current vehicle odometer: ${it.toInt()} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                            OutlinedTextField(
                                value = uiState.mileage,
                                onValueChange = viewModel::onMileageChange,
                                label = { Text("Mileage (km) at time of service*") },
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
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp), contentAlignment = Alignment.Center
                                ) {
                                    Text("No entries added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                uiState.logEntries.forEach { entry ->
                                    key(entry.id) {
                                        when (entry) {
                                            is LogEntryItem.Scheduled -> {
                                                val usedIds = uiState.logEntries.mapNotNull { (it as? LogEntryItem.Scheduled)?.selectedReminderId }.toSet()
                                                val availableTasks = uiState.availableScheduledTasks.filter {
                                                    it.typeId == entry.selectedTypeId && (it.configId !in usedIds || it.configId == entry.selectedReminderId)
                                                }
                                                ScheduledEntryCard(
                                                    entry = entry,
                                                    availableMaintenanceTypes = uiState.availableMaintenanceTypes,
                                                    availableTasks = availableTasks,
                                                    onTypeSelected = { typeId -> viewModel.onScheduledEntryTypeChanged(entry.id, typeId) },
                                                    onTaskSelected = { taskId -> viewModel.onScheduledTaskSelected(entry.id, taskId) },
                                                    onRemove = { viewModel.removeLogEntry(entry.id) },
                                                    isEnabled = isFormEnabled
                                                )
                                            }
                                            is LogEntryItem.Custom -> {
                                                CustomEntryCard(
                                                    entry = entry,
                                                    onNameChange = { newName -> viewModel.onCustomTaskNameChanged(entry.id, newName) },
                                                    onRemove = { viewModel.removeLogEntry(entry.id) },
                                                    isEnabled = isFormEnabled
                                                )
                                            }
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
                                Text(uiState.entriesError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
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
            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                        .clickable(enabled = false, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
    }
}

@Composable
private fun ScheduledEntryCard(
    entry: LogEntryItem.Scheduled,
    availableMaintenanceTypes: List<ReminderTypeResponseDto>,
    availableTasks: List<ReminderResponseDto>,
    onTypeSelected: (Int) -> Unit,
    onTaskSelected: (Int) -> Unit,
    onRemove: () -> Unit,
    isEnabled: Boolean
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scheduled Task", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, enabled = isEnabled) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DropdownSelection(
                    label = "Maintenance Type",
                    options = availableMaintenanceTypes,
                    selectedOption = availableMaintenanceTypes.find { it.id == entry.selectedTypeId },
                    onOptionSelected = { type -> onTypeSelected(type.id) },
                    optionToString = { it.name },
                    isEnabled = isEnabled
                )
                AnimatedVisibility(visible = entry.selectedTypeId != null) {
                    DropdownSelection(
                        label = "Specific Task",
                        options = availableTasks,
                        selectedOption = availableTasks.find { it.configId == entry.selectedReminderId },
                        onOptionSelected = { task -> onTaskSelected(task.configId) },
                        optionToString = { it.name },
                        isEnabled = isEnabled && availableTasks.isNotEmpty(),
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
    onNameChange: (String) -> Unit, // Acum va fi apelat la pierderea focusului
    onRemove: () -> Unit,
    isEnabled: Boolean
) {
    // --- AICI ESTE MAGIA ---
    // Creăm o stare locală, care supraviețuiește re-compunerilor,
    // pentru a ține valoarea textului.
    var text by remember(entry.name) { mutableStateOf(entry.name) }

    // Sincronizăm starea locală dacă starea din ViewModel se schimbă (ex: la adăugare/ștergere)
    LaunchedEffect(entry.name) {
        if (text != entry.name) {
            text = entry.name
        }
    }

    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Custom Task", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, enabled = isEnabled) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                },
                label = { Text("Describe Performed Task") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            onNameChange(text)
                        }
                    },
                enabled = isEnabled
            )
        }
    }
}