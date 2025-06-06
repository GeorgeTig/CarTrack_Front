package com.example.cartrack.feature.maintenance.presentation

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    appNavController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mainTabs = listOf(
        MaintenanceMainTab.ACTIVE,
        MaintenanceMainTab.INACTIVE,
        MaintenanceMainTab.WARNINGS
    )

    // Observă ciclul de viață pentru a reîmprospăta datele la revenirea pe ecran
    DisposableEffect(lifecycleOwner, uiState.selectedVehicleId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                uiState.selectedVehicleId?.let { vehicleId ->
                    Log.d("MaintenanceScreen", "Lifecycle ON_RESUME, refreshing reminders for vehicle $vehicleId.")
                    viewModel.fetchReminders(vehicleId)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Ascultă evenimentele one-time de la ViewModel (mesaje, erori)
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is MaintenanceEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- NU MAI AVEM DIALOGURI AICI, AU FOST ȘTERSE ---

    // --- Conținutul Principal al Ecranului ---
    Column(modifier = Modifier.fillMaxSize()) {
        // Bara de căutare
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
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
                shape = RoundedCornerShape(28.dp)
            )
        }

        // Tab-urile principale (Active, Inactive, Warnings)
        TabRow(selectedTabIndex = mainTabs.indexOf(uiState.selectedMainTab), modifier = Modifier.fillMaxWidth()) {
            mainTabs.forEach { tab ->
                Tab(
                    selected = uiState.selectedMainTab == tab,
                    onClick = { focusManager.clearFocus(); viewModel.selectMainTab(tab) },
                    text = { Text(tab.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        // Filtrele de tip (Chips)
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

        // Zona de conținut (listă, loading, eroare)
        Box(modifier = Modifier.fillMaxSize().weight(1f).padding(horizontal = 16.dp)) {
            when {
                uiState.isLoading && uiState.filteredReminders.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> {
                    Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
                }
                uiState.selectedVehicleId == null && !uiState.isLoading -> {
                    Text("Please select a vehicle to see maintenance reminders.", modifier = Modifier.align(Alignment.Center))
                }
                uiState.filteredReminders.isEmpty() && !uiState.isLoading -> {
                    Text("No reminders found for the current filters.", modifier = Modifier.align(Alignment.Center))
                }
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
                                        appNavController.navigate(route) // Aici se face navigarea
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