package com.example.cartrack.feature.maintenance.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceCategoryIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleCacheManager: VehicleManager // Using VehicleCacheManager interface
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val logTag = "MaintenanceVM"

    init {
        observeSelectedVehicle()
    }

    /**
     * Observes the selected vehicle ID from the cache manager.
     * When the ID changes, resets the maintenance state and fetches new reminders.
     */
    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow // Observe the StateFlow
                .distinctUntilChanged() // Only react to changes
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to $vehicleId. Resetting maintenance state.")
                    // Reset state completely for the new vehicle, keeping only the new ID
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId)
                    if (vehicleId != null) {
                        fetchReminders(vehicleId) // Fetch data for the new vehicle
                    }
                }
        }
    }

    /**
     * Fetches reminders from the repository for the given vehicle ID.
     * Updates the state with the full list, categories, and applies current filters.
     * @param vehicleId The ID of the vehicle to fetch reminders for.
     * @param isRetry Flag to indicate if this is a retry attempt after an error.
     */
    fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        // Show loading indicator unless just clearing an error on retry
        if (!isRetry || _uiState.value.error == null) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(error = null) } // Clear error on retry
        }
        Log.d(logTag, "Fetching reminders for vehicle ID: $vehicleId")

        viewModelScope.launch {
            val result = vehicleRepository.getRemindersByVehicleId(vehicleId)
            result.onSuccess { data ->
                Log.d(logTag, "Successfully fetched ${data.size} reminders.")
                // Extract unique categories from the fetched data
                val categories = data
                    .filter { it.maintenanceCategoryName.isNotBlank() } // Ensure category name is not blank
                    .distinctBy { it.maintenanceCategoryId }
                    .map {
                        CategoryFilterItem(
                            id = it.maintenanceCategoryId,
                            name = it.maintenanceCategoryName, // Use the actual name
                            icon = MaintenanceCategoryIcon.fromCategoryName(it.maintenanceCategoryName)
                        )
                    }
                    .sortedBy { it.name } // Sort categories alphabetically

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        reminders = data, // Store the full, unfiltered list
                        availableCategories = categories, // Store the extracted categories
                        // Re-apply the currently active filters to the new data
                        filteredReminders = applyFilters(
                            reminders = data,
                            query = currentState.searchQuery,
                            filterType = currentState.selectedFilterTab,
                            categoryId = currentState.selectedCategoryId
                        )
                    )
                }
            }
            result.onFailure { e ->
                Log.e(logTag, "Failed to fetch reminders: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load reminders.") }
            }
        }
    }

    /**
     * Handles changes in the search query input field.
     * Detects #Category tags and updates filters accordingly.
     * @param query The raw text entered into the search field.
     */
    fun onSearchQueryChanged(query: String) {
        val currentUiState = _uiState.value
        var newFilterType = currentUiState.selectedFilterTab
        var newCategoryId: Int? = currentUiState.selectedCategoryId
        var effectiveQuery = query // Text used for actual filtering (cleared if tag is matched)

        // Check for #Category tag (allowing spaces within the name)
        val categoryTagMatch = Regex("^#([\\w\\s]+)$").find(query.trim())
        if (categoryTagMatch != null) {
            val categoryNameFromTag = categoryTagMatch.groupValues[1].trim()
            val matchedCategory = currentUiState.availableCategories.find {
                it.name.equals(categoryNameFromTag, ignoreCase = true)
            }
            if (matchedCategory != null) {
                // Tag matched a known category
                newCategoryId = matchedCategory.id
                newFilterType = MaintenanceFilterType.CATEGORY // Switch tab logic
                effectiveQuery = "" // Clear text search component
                Log.d(logTag, "Search detected category tag: #${matchedCategory.name}, ID: $newCategoryId")
            } else {
                // Tag found but didn't match any category - treat as normal text search
                Log.d(logTag, "Search found category tag '$query' but no matching category.")
                newFilterType = MaintenanceFilterType.ALL // Revert to ALL for text search
                newCategoryId = null // Clear category ID filter
                // effectiveQuery remains the original query
            }
        } else if (query.isBlank()) {
            // Search bar cleared
            // If the Category tab *itself* was selected, keep it selected, but clear the specific category ID filter
            if (newFilterType == MaintenanceFilterType.CATEGORY) {
                newCategoryId = null // Keep CATEGORY tab active, but no specific category chosen
            } else {
                // If ALL or WARNINGS tab was active, clearing search doesn't change the tab
                newCategoryId = null
            }
        } else {
            // User is typing regular text
            // If currently on the Category tab, switch back to ALL for text searching
            if (newFilterType == MaintenanceFilterType.CATEGORY) {
                newFilterType = MaintenanceFilterType.ALL
                newCategoryId = null // Clear specific category filter
            }
        }

        // Update the UI state with the potentially modified filters and search query
        _uiState.update {
            it.copy(
                searchQuery = query, // Always store the raw query from the TextField
                selectedFilterTab = newFilterType,
                selectedCategoryId = newCategoryId,
                // Apply all current filters to the base reminder list
                filteredReminders = applyFilters(
                    reminders = it.reminders, // Filter the original full list
                    query = effectiveQuery,   // Use the processed query (blank if tag matched)
                    filterType = newFilterType,
                    categoryId = newCategoryId
                )
            )
        }
    }

    /**
     * Handles clicks on the main filter tabs (All, Warnings, Category).
     * @param filterType The tab that was clicked.
     */
    fun selectFilterTab(filterType: MaintenanceFilterType) {
        // Avoid redundant updates if the same tab is clicked (unless it's Category to maybe clear selection?)
        // Let's allow re-clicking Category to potentially clear the specific category selection later if needed.
        if (_uiState.value.selectedFilterTab == filterType && filterType != MaintenanceFilterType.CATEGORY) return

        Log.d(logTag, "Filter tab selected: $filterType")
        _uiState.update {
            it.copy(
                selectedFilterTab = filterType,
                // Clear specific category ID unless the category tab itself was just clicked
                selectedCategoryId = if (filterType == MaintenanceFilterType.CATEGORY) it.selectedCategoryId else null,
                // Clear search query only when switching *away* from Category or *to* All/Warnings
                searchQuery = if (filterType != MaintenanceFilterType.CATEGORY || it.selectedFilterTab == MaintenanceFilterType.CATEGORY) "" else it.searchQuery,
                filteredReminders = applyFilters(
                    reminders = it.reminders,
                    query = "", // Clear text query when changing main tabs (except maybe when selecting Category itself)
                    filterType = filterType,
                    categoryId = if (filterType == MaintenanceFilterType.CATEGORY) it.selectedCategoryId else null // Keep selected category if Category tab clicked
                )
            )
        }
    }

    /**
     * Handles clicks on a specific category filter chip/item.
     * Sets the main filter tab to CATEGORY, updates the search bar, and filters.
     * @param categoryId The ID of the category that was clicked.
     */
    fun selectCategoryFilter(categoryId: Int) {
        val category = _uiState.value.availableCategories.find { it.id == categoryId } ?: return // Find category details
        Log.d(logTag, "Specific Category selected: ${category.name} (ID: $categoryId)")

        // Avoid no-op if already selected
        if (_uiState.value.selectedFilterTab == MaintenanceFilterType.CATEGORY && _uiState.value.selectedCategoryId == categoryId) return

        _uiState.update {
            it.copy(
                selectedFilterTab = MaintenanceFilterType.CATEGORY, // Ensure Category tab is active
                selectedCategoryId = categoryId,                   // Set the ID
                searchQuery = "#${category.name}",                // Set search bar text
                filteredReminders = applyFilters(
                    reminders = it.reminders,
                    query = "", // No text search when category chip is clicked
                    filterType = MaintenanceFilterType.CATEGORY,
                    categoryId = categoryId // Apply category filter
                )
            )
        }
    }


    /**
     * Central filtering logic applying text search, warning filter, and category filter.
     * @param reminders The base list of reminders (usually the full list).
     * @param query The text search query (ignored if blank or just a #tag).
     * @param filterType The active main filter tab (ALL, WARNINGS, CATEGORY).
     * @param categoryId The ID of the specific category to filter by (if filterType is CATEGORY).
     * @return The final list of reminders after applying all active filters.
     */
    private fun applyFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        filterType: MaintenanceFilterType,
        categoryId: Int?
    ): List<ReminderResponseDto> {
        Log.d(logTag, "Applying filters: Query='$query', FilterType=$filterType, CategoryId=${categoryId ?: "N/A"}")

        // 1. Apply Text Filter (only if query is present and not just a tag trigger)
        val queryFiltered = if (query.isNotBlank() && !query.startsWith("#")) {
            reminders.filter {
                it.reminderName.contains(query, ignoreCase = true) ||
                        it.maintenanceTypeName.contains(query, ignoreCase = true) ||
                        it.maintenanceCategoryName.contains(query, ignoreCase = true) ||
                        it.statusName.contains(query, ignoreCase = true)
            }
        } else {
            reminders // No text filtering needed
        }

        // 2. Apply Tab/Category Filter
        val finalFiltered = when (filterType) {
            MaintenanceFilterType.ALL -> {
                queryFiltered // Only text filter applied
            }
            MaintenanceFilterType.WARNINGS -> {
                queryFiltered.filter {
                    // TODO: Adjust Status IDs based on your backend definition for warnings
                    it.statusId == 3 || it.statusId == 2 // Example: Overdue or Due Soon
                }
            }
            MaintenanceFilterType.CATEGORY -> {
                if (categoryId != null) {
                    // When filtering by category, apply it to the original list (or text filtered list if needed)
                    // Let's apply to the original list for clarity when category is explicitly chosen
                    reminders.filter { it.maintenanceCategoryId == categoryId }
                } else {
                    // Category tab selected, but no specific category yet - show all? or empty? Show all for now.
                    queryFiltered // Show text-filtered results if any, else all
                }
            }
        }
        Log.d(logTag, "Filtering resulted in ${finalFiltered.size} reminders.")
        return finalFiltered
    }

    /**
     * Public function to trigger a refresh of the reminder list for the current vehicle.
     */
    fun refreshReminders() {
        Log.d(logTag,"Refresh reminders requested.")
        // Re-fetch for the currently stored selected vehicle ID
        _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
            ?: Log.w(logTag, "Refresh requested but no vehicle is selected.")
    }
}