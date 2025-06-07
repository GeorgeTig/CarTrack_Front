package com.example.cartrack.features.maintenance

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.cards.ReminderItemCard
import com.example.cartrack.core.ui.components.FilterChipData
import com.example.cartrack.core.ui.components.TypeFilterChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    appNavController: NavHostController,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            label = { Text("Search Reminders") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search") },
            trailingIcon = { if (uiState.searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.onSearchQueryChanged("") }) { Icon(Icons.Filled.Clear, "Clear") } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(28.dp)
        )

        // Main Tabs
        TabRow(selectedTabIndex = uiState.selectedMainTab.ordinal) {
            MaintenanceMainTab.values().forEach { tab ->
                Tab(
                    selected = uiState.selectedMainTab == tab,
                    onClick = { viewModel.selectMainTab(tab) },
                    text = { Text(tab.displayName) }
                )
            }
        }

        // Filter Chips
        AnimatedVisibility(visible = uiState.availableTypes.isNotEmpty() || uiState.isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TypeFilterChip(
                        chipData = FilterChipData(-1, "All Types", Icons.Default.Build),
                        isSelected = uiState.selectedTypeId == null,
                        onClick = { viewModel.selectTypeFilter(null) }
                    )
                }
                items(uiState.availableTypes, key = { "type_${it.id}" }) { type ->
                    TypeFilterChip(
                        chipData = type,
                        isSelected = uiState.selectedTypeId == type.id,
                        onClick = { viewModel.selectTypeFilter(type.id) }
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
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onClick = {
                                    val route = viewModel.onReminderClicked(reminder.configId)
                                    appNavController.navigate(route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}