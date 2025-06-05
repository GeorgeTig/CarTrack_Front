package com.example.cartrack.feature.addmaintenance.presentation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// TIPUL principal de mentenanță (cele 9 categorii)
data class MaintenanceType(
    val id: Int,
    val name: String // Ex: "Engine", "Brakes"
) {
    override fun toString(): String = name
}

// SARCINA specifică de mentenanță (numele reminder-ului) sau opțiunea "Custom"
data class MaintenanceTask(
    val name: String, // Ex: "Oil Change", "Spark Plug Replacement", CUSTOM_TASK_NAME_OPTION
    val isCustomOption: Boolean = false // True dacă acesta este item-ul "Custom Task Name..."
) {
    override fun toString(): String = name
}

// Constanta pentru opțiunea "Custom Task" în al doilea dropdown
const val CUSTOM_TASK_NAME_OPTION = "Custom Task Name..."

// Reprezintă un item de mentenanță în UI (cu selecție în 2 pași)
data class UiMaintenanceItem(
    val id: String = UUID.randomUUID().toString(),
    var selectedMaintenanceTypeId: Int? = null,    // ID-ul TIPULUI principal selectat
    var selectedTaskName: String? = null,          // Numele SARCINII selectate (poate fi custom)
    var customTaskNameInput: String = "",          // Input pentru numele custom al SARCINII
    var showCustomTaskNameInput: Boolean = false   // Controlează vizibilitatea câmpului text pentru numele custom al SARCINII
)

// State-ul UI pentru ecran
data class AddMaintenanceUiState(
    val currentVehicleId: Int? = null,
    val currentVehicleSeries: String = "Vehicle",
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mileage: String = "",
    val serviceProvider: String = "",
    val notes: String = "",
    val cost: String = "",
    val maintenanceItems: List<UiMaintenanceItem> = emptyList(),
    val availableMaintenanceTypes: List<MaintenanceType> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val mileageError: String? = null,
    val costError: String? = null,
    val dateError: String? = null,
    val itemErrors: Map<String, String?> = emptyMap() // Erori per UiMaintenanceItem.id (cheia e item.id)
)

// Funcție helper pentru a crea un item nou
fun createNewUiMaintenanceItem(): UiMaintenanceItem {
    // La creare, nu are niciun tip selectat, și niciun task.
    // Câmpul custom pentru nume task nu e vizibil inițial.
    return UiMaintenanceItem(
        selectedMaintenanceTypeId = null, // Începe fără tip selectat
        selectedTaskName = null,
        customTaskNameInput = "",
        showCustomTaskNameInput = false
    )
}
