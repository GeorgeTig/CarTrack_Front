package com.example.cartrack.feature.maintenance.presentation

import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto


enum class MaintenanceFilterType {
    ALL, WARNINGS, TYPE
}

data class TypeFilterItem(
    val id: Int, // Corresponds to typeId
    val name: String, // Corresponds to typeName
    val icon: MaintenanceTypeIcon
)

data class EditReminderFormState(
    val reminderToEdit: ReminderResponseDto? = null,
    val nameInput: String = "",
    val mileageIntervalInput: String = "",
    val timeIntervalInput: String = "",
    val nameError: String? = null,
    val mileageIntervalError: String? = null,
    val timeIntervalError: String? = null
)

data class MaintenanceUiState(
    val isLoading: Boolean = false, // General loading for list or major operations
    val selectedVehicleId: Int? = null,
    val searchQuery: String = "",
    val reminders: List<ReminderResponseDto> = emptyList(), // Full list from API
    val filteredReminders: List<ReminderResponseDto> = emptyList(), // List displayed after filters
    val error: String? = null, // General error for the screen
    val selectedFilterTab: MaintenanceFilterType = MaintenanceFilterType.ALL,
    val availableTypes: List<TypeFilterItem> = emptyList(),
    val selectedTypeId: Int? = null,
    val reminderForDetailView: ReminderResponseDto? = null, // For detail dialog
    val isEditDialogVisible: Boolean = false, // For edit dialog visibility
    val editFormState: EditReminderFormState = EditReminderFormState()
)

sealed class MaintenanceEvent {
    data class ShowMessage(val message: String) : MaintenanceEvent()
    data class ShowError(val message: String) : MaintenanceEvent()
}