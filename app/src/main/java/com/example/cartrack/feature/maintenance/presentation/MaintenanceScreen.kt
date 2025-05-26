package com.example.cartrack.feature.maintenance.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.core.ui.TypeFilterChip
import com.example.cartrack.core.ui.cards.ReminderCard.EditReminderDialog
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderDetailCard
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val filterTabs = listOf(
        MaintenanceFilterType.ALL,
        MaintenanceFilterType.WARNINGS,
        MaintenanceFilterType.TYPE
    )

    // Handle one-time events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is MaintenanceEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Dialog for Reminder Details ---
    if (uiState.reminderForDetailView != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReminderDetails() },
            text = { ReminderDetailCard(reminder = uiState.reminderForDetailView!!) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(onClick = { viewModel.toggleReminderActiveStatus() }) { Text("Toggle Active") }
                    OutlinedButton(onClick = { viewModel.showEditReminderDialog() }) { Text("Edit") }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReminderDetails() }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }

    // --- Dialog for Editing Reminder ---
    if (uiState.isEditDialogVisible) {
        EditReminderDialog( // Assuming EditReminderDialog is defined and imported
            formState = uiState.editFormState,
            onDismiss = { viewModel.dismissEditReminderDialog() },
            onNameChange = viewModel::onEditNameChanged,
            onMileageIntervalChange = viewModel::onEditMileageIntervalChanged,
            onTimeIntervalChange = viewModel::onEditTimeIntervalChanged,
            onSave = { viewModel.saveReminderEdits() },
            onRestoreDefaults = { viewModel.restoreReminderToDefaults() }
        )
    }

    // --- Main Screen Content ---
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Row: Search Bar and Settings
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Search or use #Type") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { Toast.makeText(context, "Settings Clicked", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        // Main Filter TabRow
        TabRow(
            selectedTabIndex = filterTabs.indexOf(uiState.selectedFilterTab),
            modifier = Modifier.fillMaxWidth()
        ) {
            filterTabs.forEach { tabType ->
                Tab(
                    selected = uiState.selectedFilterTab == tabType,
                    onClick = { focusManager.clearFocus(); viewModel.selectFilterTab(tabType) },
                    text = { Text(tabType.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        // Conditional Secondary Row for Type Filters
        AnimatedVisibility(
            visible = uiState.selectedFilterTab == MaintenanceFilterType.TYPE && uiState.availableTypes.isNotEmpty(),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableTypes, key = { it.id }) { typeItem ->
                    TypeFilterChip( // Assuming TypeFilterChip composable exists
                        type = typeItem,
                        isSelected = uiState.selectedTypeId == typeItem.id,
                        onClick = { focusManager.clearFocus(); viewModel.selectTypeFilter(typeItem.id) }
                    )
                }
            }
        }

        // Content Area: Loading, Error, or List of Reminders
        Box(
            modifier = Modifier
                .fillMaxSize() // Take remaining space
                .padding(horizontal = 16.dp) // Horizontal padding for the content inside the Box
        ) {
            when {
                // Show loading if initial list is loading AND no reminders are currently shown
                uiState.isLoading && uiState.filteredReminders.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Show error if an error occurred
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refreshReminders() }) { Text("Retry") }
                    }
                }
                // Show message if no vehicle is selected yet
                uiState.selectedVehicleId == null -> {
                    Text(
                        "Please select a vehicle to view reminders.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Show message if filters result in an empty list
                uiState.filteredReminders.isEmpty() && (uiState.searchQuery.isNotEmpty() || uiState.selectedFilterTab != MaintenanceFilterType.ALL || uiState.selectedTypeId != null) -> {
                    val filterContext = when (uiState.selectedFilterTab) {
                        MaintenanceFilterType.WARNINGS -> "warnings"
                        MaintenanceFilterType.TYPE -> "'#${uiState.availableTypes.find{ it.id == uiState.selectedTypeId}?.name ?: uiState.searchQuery}'"
                        else -> if (uiState.searchQuery.isNotEmpty()) "'${uiState.searchQuery}'" else "current filters"
                    }
                    Text(
                        "No reminders match $filterContext.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Show message if there are no reminders at all for the vehicle (and not due to filters)
                uiState.reminders.isEmpty() && !uiState.isLoading -> {
                    Text(
                        "No maintenance reminders for this vehicle yet.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Display the list of reminders
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp) // Padding for the list items
                    ) {
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            ReminderItemCard( // Assuming ReminderItemCard is defined and imported
                                reminder = reminder,
                                onClick = { viewModel.showReminderDetails(reminder) }
                            )
                        }
                    }
                }
            }
        } // End Content Box
    } // End Main Column
}