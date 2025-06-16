package com.example.cartrack.features.add_maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

sealed class LogEntryItem(val id: String = UUID.randomUUID().toString()) {
    data class Scheduled(
        val selectedTypeId: Int? = null,
        val selectedReminderId: Int? = null
    ) : LogEntryItem()

    data class Custom(
        val name: String = ""
    ) : LogEntryItem()
}

data class AddMaintenanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,

    val currentVehicleId: Int? = null,
    val currentVehicleSeries: String = "Vehicle",

    val availableScheduledTasks: List<ReminderResponseDto> = emptyList(),
    val availableMaintenanceTypes: List<ReminderTypeResponseDto> = emptyList(),

    val logEntries: List<LogEntryItem> = emptyList(),

    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mileage: String = "",
    val serviceProvider: String = "",
    val notes: String = "",
    val cost: String = "",

    val mileageError: String? = null,
    val entriesError: String? = null
)

sealed class AddMaintenanceEvent {
    data class ShowToast(val message: String) : AddMaintenanceEvent()
    object NavigateBack : AddMaintenanceEvent()
}