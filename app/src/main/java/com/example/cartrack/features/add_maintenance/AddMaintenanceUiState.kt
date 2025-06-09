package com.example.cartrack.features.add_maintenance

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// --- MODELE SPECIFICE PENTRU STAREA UI ---

/**
 * Reprezintă un reminder din lista de la backend, dar cu o proprietate
 * în plus pentru a ține minte dacă este bifat în UI.
 */
data class SelectableReminder(
    val reminder: ReminderResponseDto,
    var isSelected: Boolean = false
)

/**
 * Reprezintă un câmp de text pentru o lucrare custom, având un ID unic
 * pentru a-l putea identifica în listă la modificare sau ștergere.
 */
data class CustomTask(
    val id: String = UUID.randomUUID().toString(),
    var name: String = ""
)


// --- STAREA PRINCIPALĂ A UI-ului PENTRU ACEST ECRAN ---

data class AddMaintenanceUiState(
    // Stări de încărcare și salvare
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    // Date despre vehiculul curent
    val currentVehicleId: Int? = null,
    val currentVehicleSeries: String = "Vehicle",

    // Datele principale ale formularului
    val selectableReminders: List<SelectableReminder> = emptyList(),
    val customTasks: List<CustomTask> = listOf(CustomTask()), // Întotdeauna începe cu un câmp gol

    // Câmpuri generale ale formularului
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mileage: String = "",
    val serviceProvider: String = "",
    val notes: String = "",
    val cost: String = "",

    // Erori de validare afișate utilizatorului
    val error: String? = null, // Eroare generală (ex: de rețea)
    val mileageError: String? = null, // Eroare specifică de la backend pentru kilometraj
    val tasksError: String? = null // Eroare dacă nu se selectează nicio lucrare
)


// --- EVENIMENTE ONE-SHOT DE LA VIEWMODEL CĂTRE UI ---

sealed class AddMaintenanceEvent {
    data class ShowToast(val message: String) : AddMaintenanceEvent()
    object NavigateBack : AddMaintenanceEvent()
}