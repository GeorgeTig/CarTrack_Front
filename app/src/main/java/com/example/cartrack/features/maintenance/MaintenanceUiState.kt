package com.example.cartrack.features.maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.ui.components.FilterChipData

// Enum pentru tab-urile principale
enum class MaintenanceMainTab(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    WARNINGS("Warnings")
}

// Starea principală a UI-ului pentru ecranul de mentenanță
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

// Evenimente one-time de la ViewModel către UI
sealed class MaintenanceEvent {
    data class ShowMessage(val message: String) : MaintenanceEvent()
}