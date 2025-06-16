package com.example.cartrack.features.maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.ui.components.FilterChipData

enum class MaintenanceMainTab(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    WARNINGS("Warnings")
}

data class MaintenanceUiState(
    val isLoading: Boolean = true,
    val selectedVehicleId: Int? = null,
    val searchQuery: String = "",
    val reminders: List<ReminderResponseDto> = emptyList(),
    val filteredReminders: List<ReminderResponseDto> = emptyList(),
    val error: String? = null,
    val selectedMainTab: MaintenanceMainTab = MaintenanceMainTab.ACTIVE,
    val availableTypes: List<FilterChipData> = emptyList(),
    val selectedTypeId: Int? = null,
)

sealed class MaintenanceEvent {
    data class ShowMessage(val message: String) : MaintenanceEvent()
    // --- EVENIMENT NOU ---
    data class NavigateToReminderDetail(val reminderId: Int) : MaintenanceEvent()
}