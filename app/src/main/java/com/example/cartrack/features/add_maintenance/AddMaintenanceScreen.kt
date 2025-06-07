package com.example.cartrack.features.add_maintenance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceGeneralInfoSection
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceOptionalInfoSection
import com.example.cartrack.features.add_maintenance.helpers.MaintenanceTaskItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    navController: NavHostController,
    viewModel: AddMaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isFormEnabled = !uiState.isLoading && !uiState.isSaving

    LaunchedEffect(uiState.saveSuccess, uiState.error) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Maintenance log saved!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveStatus()
            navController.popBackStack()
        }
        uiState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.resetSaveStatus()
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
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.availableMaintenanceTypes.isEmpty()) {
                // Afișează eroarea doar dacă nu s-au putut încărca datele inițiale
                Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center).padding(16.dp))
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text("Service Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        MaintenanceGeneralInfoSection(
                            date = uiState.date,
                            dateError = uiState.dateError,
                            onDateChange = viewModel::onDateChange,
                            mileage = uiState.mileage,
                            mileageError = uiState.mileageError,
                            onMileageChange = viewModel::onMileageChange,
                            isEnabled = isFormEnabled
                        )
                    }
                    item { Text("Performed Tasks", style = MaterialTheme.typography.titleLarge) }

                    itemsIndexed(uiState.maintenanceItems, key = { _, item -> item.id }) { index, item ->
                        MaintenanceTaskItemCard(
                            item = item,
                            index = index,
                            availableMaintenanceTypes = uiState.availableMaintenanceTypes,
                            getTasksForSelectedType = viewModel::getTasksForType,
                            itemError = uiState.itemErrors[item.id],
                            onTypeSelected = { type -> viewModel.onMaintenanceTypeSelected(item.id, type) },
                            onTaskSelected = { task -> viewModel.onMaintenanceTaskSelected(item.id, task) },
                            onCustomTaskNameChanged = { name -> viewModel.onCustomTaskNameChanged(item.id, name) },
                            onRemoveItem = { if (uiState.maintenanceItems.size > 1) viewModel.removeMaintenanceItem(item.id) },
                            isEnabled = isFormEnabled
                        )
                    }

                    item {
                        Button(onClick = viewModel::addMaintenanceItem, modifier = Modifier.fillMaxWidth(), enabled = isFormEnabled) {
                            Icon(Icons.Filled.Add, "Add Task")
                            Spacer(Modifier.width(8.dp))
                            Text("Add Another Task")
                        }
                    }

                    item {
                        Text("Additional Info (Optional)", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        MaintenanceOptionalInfoSection(
                            serviceProvider = uiState.serviceProvider,
                            onServiceProviderChange = viewModel::onServiceProviderChange,
                            cost = uiState.cost,
                            costError = uiState.costError,
                            onCostChange = viewModel::onCostChange,
                            notes = uiState.notes,
                            onNotesChange = viewModel::onNotesChange,
                            isEnabled = isFormEnabled
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) } // Spațiu pentru FAB
                }
            }

            if (uiState.isSaving) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(false){}, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}