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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
// --- Import renamed TypeFilterChip ---
import com.example.cartrack.core.ui.TypeFilterChip // Adjust import path if needed
// --- Import ReminderItemCard which uses the NEW DTO ---
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // --- Updated Tab List ---
    val filterTabs = listOf(
        MaintenanceFilterType.ALL,
        MaintenanceFilterType.WARNINGS,
        MaintenanceFilterType.TYPE // Use TYPE
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Top Row for Search Bar and Settings ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.weight(1f),
                label = { Text("Search or use #Type") }, // Updated label hint
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
            IconButton(onClick = { /* TODO: Settings Action */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        } // End Top Row

        // --- Main Filter Tab Row ---
        TabRow(
            selectedTabIndex = filterTabs.indexOf(uiState.selectedFilterTab),
            modifier = Modifier.fillMaxWidth(),
        ) {
            filterTabs.forEach { tabType ->
                Tab(
                    selected = uiState.selectedFilterTab == tabType,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.selectFilterTab(tabType)
                    },
                    // Updated Tab Text based on enum name
                    text = { Text(tabType.name.replaceFirstChar { it.titlecase() }) }
                )
            }
        } // End Main Tab Row

        // --- CONDITIONAL Secondary Row for Types ---
        AnimatedVisibility(
            // Show if TYPE tab selected and types available
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
                // Use availableTypes and selectedTypeId
                items(uiState.availableTypes, key = { it.id }) { typeItem ->
                    // --- Call renamed TypeFilterChip ---
                    TypeFilterChip(
                        type = typeItem, // Pass TypeFilterItem
                        isSelected = uiState.selectedTypeId == typeItem.id,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.selectTypeFilter(typeItem.id) // Call selectTypeFilter
                        }
                    )
                }
            }
        } // End AnimatedVisibility for Type Row

        // --- Content Area (Loading, Error, List) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                uiState.error != null -> { /* ... Error display ... */ }
                uiState.selectedVehicleId == null -> { /* ... No vehicle message ... */ }
                // Updated empty filter message logic
                uiState.filteredReminders.isEmpty() && (uiState.searchQuery.isNotEmpty() || uiState.selectedFilterTab != MaintenanceFilterType.ALL || uiState.selectedTypeId != null) -> {
                    Text(
                        text = when (uiState.selectedFilterTab) {
                            MaintenanceFilterType.WARNINGS -> "No reminders match warnings."
                            MaintenanceFilterType.TYPE -> "No reminders found for '#${uiState.availableTypes.find{ it.id == uiState.selectedTypeId}?.name ?: ""}'." // Use type name
                            else -> "No reminders found for your search."
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.reminders.isEmpty() && !uiState.isLoading -> { /* ... No reminders exist message ... */ }
                else -> {
                    // List of Reminders
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        // Use filteredReminders which holds List<NEW DTO>
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            // ReminderItemCard needs to be updated to use the NEW DTO
                            ReminderItemCard(reminder = reminder)
                        }
                    }
                }
            } // End Content When
        } // End Content Box
    } // End Main Column
}