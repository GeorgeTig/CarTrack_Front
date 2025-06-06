package com.example.cartrack.feature.maintenance.presentation

import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto

// Main Tabs
enum class MaintenanceMainTab {
    ACTIVE, INACTIVE, WARNINGS
}

// Type Filter item
data class TypeFilterItem(
    val id: Int,
    val name: String,
    val icon: MaintenanceTypeIcon
)

// Edit Dialog Form State
data class EditReminderFormState(
    val reminderToEdit: ReminderResponseDto? = null,
    val nameInput: String = "",
    val mileageIntervalInput: String = "",
    val timeIntervalInput: String = "",
    val nameError: String? = null,
    val mileageIntervalError: String? = null,
    val timeIntervalError: String? = null
)

// Main UI State for Maintenance Screen
data class MaintenanceUiState(
    val isLoading: Boolean = false,
    val selectedVehicleId: Int? = null,
    val searchQuery: String = "",
    val reminders: List<ReminderResponseDto> = emptyList(),
    val filteredReminders: List<ReminderResponseDto> = emptyList(),
    val error: String? = null,
    val selectedMainTab: MaintenanceMainTab = MaintenanceMainTab.ACTIVE,
    val availableTypes: List<TypeFilterItem> = emptyList(),
    val selectedTypeId: Int? = null,
    val reminderForDetailView: ReminderResponseDto? = null,
    val isEditDialogVisible: Boolean = false,
    val editFormState: EditReminderFormState = EditReminderFormState()
)

// Events from ViewModel to UI
sealed class MaintenanceEvent {
    data class ShowMessage(val message: String) : MaintenanceEvent()
    data class ShowError(val message: String) : MaintenanceEvent()
}