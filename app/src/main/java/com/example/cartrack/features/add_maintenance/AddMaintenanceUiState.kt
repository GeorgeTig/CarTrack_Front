package com.example.cartrack.features.add_maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.maintenance.ReminderTypeResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// ... LogEntryItem și AddMaintenanceUiState data class ...
// (Acestea rămân la fel cum le-ai avut)

sealed class LogEntryItem(val id: String = UUID.randomUUID().toString()) {
    // Această clasă este stabilă
    data class Scheduled(
        val selectedTypeId: Int? = null,
        val selectedReminderId: Int? = null
    ) : LogEntryItem()

    // Această clasă este stabilă
    data class Custom(
        val name: String = ""
    ) : LogEntryItem()
}

data class AddMaintenanceUiState(
    val logEntries: List<LogEntryItem> = emptyList(), // Asigură-te că este List, nu SnapshotStateList
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val currentVehicleId: Int? = null,
    val currentVehicleSeries: String = "Vehicle",
    val currentVehicleMileage: Double? = null, // Am adăugat asta în discuția anterioară
    val availableScheduledTasks: List<ReminderResponseDto> = emptyList(),
    val availableMaintenanceTypes: List<ReminderTypeResponseDto> = emptyList(),
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mileage: String = "",
    val serviceProvider: String = "",
    val notes: String = "",
    val cost: String = "",
    val mileageError: String? = null,
    val entriesError: String? = null
)


// --- AICI ESTE PARTEA IMPORTANTĂ ---
sealed class AddMaintenanceEvent {
    data class ShowToast(val message: String) : AddMaintenanceEvent()
    object NavigateBackOnSuccess : AddMaintenanceEvent() // Eveniment separat pentru navigare
}