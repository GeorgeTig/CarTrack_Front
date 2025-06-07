package com.example.cartrack.features.maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.ui.components.FilterChipData

// Main Tabs for filtering reminders by status
enum class MaintenanceMainTab {
    ACTIVE, INACTIVE, WARNINGS
}

// Main UI State for Maintenance Screen
data class MaintenanceUiState(
    val isLoading: Boolean = false,
    val selectedVehicleId: Int? = null,
    val searchQuery: String = "",
    val reminders: List<ReminderResponseDto> = emptyList(),
    val filteredReminders: List<ReminderResponseDto> = emptyList(),
    val error: String? = null,
    val selectedMainTab: MaintenanceMainTab = MaintenanceMainTab.ACTIVE,
    val availableTypes: List<FilterChipData> = emptyList(),
    val selectedTypeId: Int? = null,
)

// Events from ViewModel to UI for one-time actions
sealed class MaintenanceEvent {
    data class ShowMessage(val message: String) : MaintenanceEvent()
}