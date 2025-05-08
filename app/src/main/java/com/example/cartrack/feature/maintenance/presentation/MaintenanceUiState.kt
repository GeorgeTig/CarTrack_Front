package com.example.cartrack.feature.maintenance.presentation

import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto


// Enum for the filter tabs (defined earlier, ensure accessible)
enum class MaintenanceFilterType {
    ALL, WARNINGS, TYPE
}

// Data class for selectable category filter item (defined earlier, ensure accessible)
data class TypeFilterItem(
    val id: Int,
    val name: String,
    val icon: MaintenanceTypeIcon
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
    val availableTypes: List<TypeFilterItem> = emptyList(), // Unique categories from reminders
    val selectedTypeId: Int? = null // ID of category selected via filter tab/search
)