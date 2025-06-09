package com.example.cartrack.features.add_maintenance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.add_maintenance.components.CustomTasksSection
import com.example.cartrack.features.add_maintenance.components.SelectableRemindersSection
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceGeneralInfoSection
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceOptionalInfoSection

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
                is AddMaintenanceEvent.ShowToast -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
                is AddMaintenanceEvent.NavigateBack -> {
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
                FloatingActionButton(
                    onClick = { viewModel.saveMaintenance() },
                    containerColor = if(isFormEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if(isFormEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(Icons.Filled.Save, "Save Maintenance Log")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.selectableReminders.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchInitialData() }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            MaintenanceGeneralInfoSection(
                                date = uiState.date,
                                onDateChange = viewModel::onDateChange,
                                mileage = uiState.mileage,
                                mileageError = uiState.mileageError,
                                onMileageChange = viewModel::onMileageChange,
                                isEnabled = isFormEnabled
                            )
                        }

                        item {
                            SelectableRemindersSection(
                                reminders = uiState.selectableReminders,
                                onReminderToggled = viewModel::onReminderToggled,
                                isEnabled = isFormEnabled
                            )
                        }

                        item {
                            CustomTasksSection(
                                tasks = uiState.customTasks,
                                onTaskChanged = viewModel::onCustomTaskChanged,
                                onAddTask = viewModel::addCustomTaskField,
                                onRemoveTask = viewModel::removeCustomTaskField,
                                isEnabled = isFormEnabled
                            )
                            uiState.tasksError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }

                        item {
                            MaintenanceOptionalInfoSection(
                                serviceProvider = uiState.serviceProvider,
                                onServiceProviderChange = viewModel::onServiceProviderChange,
                                cost = uiState.cost,
                                costError = null,
                                onCostChange = viewModel::onCostChange,
                                notes = uiState.notes,
                                onNotesChange = viewModel::onNotesChange,
                                isEnabled = isFormEnabled
                            )
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }

            if (uiState.isSaving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}