package com.example.cartrack.feature.maintenance.presentation

import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceCategoryIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto


// Enum for the filter tabs (defined earlier, ensure accessible)
enum class MaintenanceFilterType {
    ALL, WARNINGS, CATEGORY
}

// Data class for selectable category filter item (defined earlier, ensure accessible)
data class CategoryFilterItem(
    val id: Int,
    val name: String,
    val icon: MaintenanceCategoryIcon
)

// UI State data class (defined earlier, ensure accessible)
data class MaintenanceUiState(
    val isLoading: Boolean = false,
    val selectedVehicleId: Int? = null,
    val searchQuery: String = "",
    val reminders: List<ReminderResponseDto> = emptyList(), // Original list from API
    val filteredReminders: List<ReminderResponseDto> = emptyList(), // List displayed after ALL filters
    val error: String? = null,
    val selectedFilterTab: MaintenanceFilterType = MaintenanceFilterType.ALL, // Default filter
    val availableCategories: List<CategoryFilterItem> = emptyList(), // Unique categories from reminders
    val selectedCategoryId: Int? = null // ID of category selected via filter tab/search
)