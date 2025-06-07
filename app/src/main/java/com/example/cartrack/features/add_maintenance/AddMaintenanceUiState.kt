package com.example.cartrack.features.add_maintenance

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// Model pentru un TIP principal de mentenanță (ex: "Engine", "Brakes")
data class MaintenanceType(
    val id: Int,
    val name: String
)

// Model pentru o SARCINĂ specifică (ex: "Oil Change")
data class MaintenanceTask(
    val name: String,
    val isCustomOption: Boolean = false
)

const val CUSTOM_TASK_NAME_OPTION = "Custom Task Name..."

// Reprezintă un item dinamic în UI
data class UiMaintenanceItem(
    val id: String = UUID.randomUUID().toString(),
    var selectedMaintenanceTypeId: Int? = null,
    var selectedTaskName: String? = null,
    var customTaskNameInput: String = "",
    var showCustomTaskNameInput: Boolean = false
)

// Starea principală a UI-ului pentru acest ecran
data class AddMaintenanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Date generale
    val currentVehicleId: Int? = null,
    val currentVehicleSeries: String = "Vehicle",
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mileage: String = "",
    val serviceProvider: String = "",
    val notes: String = "",
    val cost: String = "",

    // Managementul listei dinamice
    val maintenanceItems: List<UiMaintenanceItem> = emptyList(),
    val availableMaintenanceTypes: List<MaintenanceType> = emptyList(),

    // Erori
    val error: String? = null,
    val dateError: String? = null,
    val mileageError: String? = null,
    val costError: String? = null,
    val itemErrors: Map<String, String?> = emptyMap(), // Erori per item, cheia este UiMaintenanceItem.id

    val saveSuccess: Boolean = false
)