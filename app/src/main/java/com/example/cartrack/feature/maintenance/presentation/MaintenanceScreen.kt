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
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.TypeFilterChip
import com.example.cartrack.core.ui.cards.ReminderCard.ActivateReminderDialog
// Ensure ReminderDetailCard is imported correctly
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderDetailCard
// Ensure EditReminderDialog is imported correctly
import com.example.cartrack.core.ui.cards.ReminderCard.EditReminderDialog
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderItemCard
import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon // For "All Types" chip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = hiltViewModel(),
    appNavController: NavHostController // Primește controller-ul
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val mainTabs = listOf(
        MaintenanceMainTab.ACTIVE,
        MaintenanceMainTab.INACTIVE,
        MaintenanceMainTab.WARNINGS
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is MaintenanceEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Dialog for Reminder Details (for ACTIVE reminders) ---
    if (uiState.reminderForDetailView != null) {
        val activeReminderInDialog = uiState.reminderForDetailView!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissReminderDetails() },
            icon = {
                val typeIconInfo = MaintenanceTypeIcon.fromTypeId(activeReminderInDialog.typeId)
                Icon(typeIconInfo.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text(activeReminderInDialog.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = { ReminderDetailCard(reminder = activeReminderInDialog) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // Toggle Active/Inactive Button (Text dynamically changes)
                    TextButton(
                        onClick = { viewModel.toggleReminderActiveStatus(activeReminderInDialog.configId) },
                        enabled = !uiState.isLoading // Disable if a general action is loading
                    ) { Text(if (activeReminderInDialog.isActive) "Set Inactive" else "Set Active") }

                    Button(
                        onClick = { viewModel.showEditReminderDialog() },
                        enabled = !uiState.isLoading && activeReminderInDialog.isEditable // Only if editable and not loading
                    ) { Text("Edit") }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissReminderDetails() },
                    enabled = !uiState.isLoading
                ) { Text("Close") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    // --- NEW Dialog for Activating an INACTIVE Reminder ---
    if (uiState.reminderToActivate != null) {
        ActivateReminderDialog( // Call the new dialog composable
            reminder = uiState.reminderToActivate!!,
            onDismiss = { viewModel.dismissActivateReminderDialog() },
            onConfirmActivate = {
                // Call the existing toggle function which will activate it
                viewModel.toggleReminderActiveStatus(uiState.reminderToActivate!!.configId)
                // The ViewModel's toggle function already handles dismissing this dialog via state update
            },
            isLoading = uiState.isLoading // Use general loading flag for dialog buttons
        )
    }

    // Dialog for Editing Reminder (no changes to its call)
    if (uiState.isEditDialogVisible) {
        EditReminderDialog(
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
        // Top Row: Search Bar (Settings Button Removed)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(), // Search bar takes full width
                label = { Text("Search Reminders") },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) { Icon(Icons.Filled.Clear, "Clear") }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(28.dp) // Curvy search bar
            )
        }

        // Main Filter TabRow
        TabRow(selectedTabIndex = mainTabs.indexOf(uiState.selectedMainTab), modifier = Modifier.fillMaxWidth()) {
            mainTabs.forEach { tab ->
                Tab(
                    selected = uiState.selectedMainTab == tab,
                    onClick = { focusManager.clearFocus(); viewModel.selectMainTab(tab) },
                    text = { Text(tab.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        // Secondary Row for Type Filters
        AnimatedVisibility(
            visible = uiState.availableTypes.isNotEmpty() || uiState.isLoading,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                item {
                    TypeFilterChip(
                        type = TypeFilterItem(id = -1, name = "All Types", icon = MaintenanceTypeIcon.OTHER),
                        isSelected = uiState.selectedTypeId == null,
                        onClick = { focusManager.clearFocus(); viewModel.selectTypeFilter(null) }
                    )
                }
                items(uiState.availableTypes, key = { "type_${it.id}" }) { typeItem ->
                    TypeFilterChip(
                        type = typeItem,
                        isSelected = uiState.selectedTypeId == typeItem.id,
                        onClick = { focusManager.clearFocus(); viewModel.selectTypeFilter(typeItem.id) }
                    )
                }
            }
        }

        // Content Area: Loading, Error, or List of Reminders
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp)) {
            when {
                uiState.isLoading && uiState.filteredReminders.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> { /* ... Error display ... */ }
                uiState.selectedVehicleId == null && !uiState.isLoading -> { /* ... No vehicle selected ... */ }
                uiState.filteredReminders.isEmpty() && !uiState.isLoading -> { /* ... Empty list message based on filters ... */ }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onClick = {
                                    // Apelăm funcția din ViewModel și îi pasăm lambda-ul de navigare
                                    viewModel.onReminderItemClicked(reminder) { route ->
                                        appNavController.navigate(route) // Aici folosești navController-ul global
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}