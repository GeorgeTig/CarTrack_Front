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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.core.ui.CategoryFilterChip
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderItemCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Define the main filter tabs
    val filterTabs = listOf(
        MaintenanceFilterType.ALL,
        MaintenanceFilterType.WARNINGS,
        MaintenanceFilterType.CATEGORY // Add CATEGORY tab here
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
                label = { Text("Search or use #Category") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = { /* ... Clear button ... */ },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* Settings Action */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        } // End Top Row

        // --- Main Filter Tab Row ---
        TabRow(
            selectedTabIndex = filterTabs.indexOf(uiState.selectedFilterTab), // Index based on selected tab
            modifier = Modifier.fillMaxWidth(),
            // containerColor = MaterialTheme.colorScheme.surfaceVariant // Optional background
        ) {
            filterTabs.forEach { tabType ->
                Tab(
                    selected = uiState.selectedFilterTab == tabType,
                    onClick = {
                        focusManager.clearFocus() // Clear focus when changing tabs
                        viewModel.selectFilterTab(tabType)
                    },
                    text = { Text(tabType.name.replaceFirstChar { it.titlecase() }) }
                    // Optional: Add icons to tabs
                )
            }
        } // End Main Tab Row

        // --- CONDITIONAL Secondary Row for Categories ---
        AnimatedVisibility(
            visible = uiState.selectedFilterTab == MaintenanceFilterType.CATEGORY && uiState.availableCategories.isNotEmpty(),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), // Subtle background
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableCategories, key = { it.id }) { category ->
                    CategoryFilterChip(
                        category = category,
                        isSelected = uiState.selectedCategoryId == category.id,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.selectCategoryFilter(category.id)
                        }
                    )
                }
            }
        } // End AnimatedVisibility for Category Row

        // --- Content Area (Loading, Error, List) ---
        Box(
            modifier = Modifier
                .fillMaxSize() // Takes remaining space
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                uiState.error != null -> { /* ... Error display ... */ }
                uiState.selectedVehicleId == null -> { /* ... No vehicle message ... */ }
                uiState.filteredReminders.isEmpty() && (uiState.searchQuery.isNotEmpty() || uiState.selectedFilterTab != MaintenanceFilterType.ALL || uiState.selectedCategoryId != null) -> {
                    /* ... Empty filter results message ... */
                    Text(
                        text = when (uiState.selectedFilterTab) {
                            MaintenanceFilterType.WARNINGS -> "No reminders match warnings."
                            MaintenanceFilterType.CATEGORY -> "No reminders found for '#${uiState.availableCategories.find{ it.id == uiState.selectedCategoryId}?.name ?: ""}'."
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
                        items(uiState.filteredReminders, key = { it.configId }) { reminder ->
                            ReminderItemCard(reminder = reminder)
                        }
                    }
                }
            } // End Content When
        } // End Content Box
    } // End Main Column
}