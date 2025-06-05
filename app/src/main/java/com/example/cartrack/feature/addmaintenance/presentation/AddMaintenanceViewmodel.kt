package com.example.cartrack.feature.addmaintenance.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder // Import pentru a obține client ID
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import com.example.cartrack.feature.addmaintenance.data.MaintenanceItemDto
import com.example.cartrack.feature.addmaintenance.data.MaintenanceSaveRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddMaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager,
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMaintenanceUiState())
    val uiState: StateFlow<AddMaintenanceUiState> = _uiState.asStateFlow()

    private val logTag = "AddMaintenanceVM"

    private var processedTasksByTypeIdMap: Map<Int, List<MaintenanceTask>> = emptyMap()

    // Lista tipurilor principale de mentenanță (cu ID 0 pentru placeholder)
    private val mainMaintenanceTypesWithPlaceholder = listOf(
        MaintenanceType(0, "Select Category...") // Placeholder
    ) + listOf( // Tipurile reale
        MaintenanceType(1, "Engine"),
        MaintenanceType(2, "Brakes"),
        MaintenanceType(3, "Fluids & Filters"),
        MaintenanceType(4, "Tires & Wheels"),
        MaintenanceType(5, "Suspension & Steering"),
        MaintenanceType(6, "Electrical System"),
        MaintenanceType(7, "HVAC System"),
        MaintenanceType(8, "Body & Exterior"),
        MaintenanceType(9, "Inspection & General Check")
    )


    init {
        Log.d(logTag, "ViewModel initialized. Fetching initial data...")
        fetchInitialData()
    }

    private fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            var vehicleSeriesName = _uiState.value.currentVehicleSeries
            var dataFetchError: String? = null
            var fetchedRemindersSuccessfully = false

            if (vehicleId != null) {
                Log.d(logTag, "Current vehicle ID: $vehicleId. Fetching details...")
                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId != null) {
                    vehicleRepository.getVehiclesByClientId(clientId)
                        .getOrNull()?.find { it.id == vehicleId }?.let {
                            vehicleSeriesName = it.series
                            Log.d(logTag, "Fetched vehicle series: $vehicleSeriesName")
                        } ?: Log.w(logTag, "Vehicle with ID $vehicleId not found for client $clientId.")

                    val remindersResult = vehicleRepository.getRemindersByVehicleId(vehicleId)
                    remindersResult.onSuccess { reminders ->
                        Log.d(logTag, "Fetched ${reminders.size} reminders for vehicle $vehicleId.")
                        processRemindersAndPopulateTaskMap(reminders) // Doar populează map-ul intern
                        fetchedRemindersSuccessfully = true
                    }.onFailure { e ->
                        Log.e(logTag, "Failed to fetch reminders for vehicle $vehicleId: ${e.message}")
                        dataFetchError = "Could not load maintenance options: ${e.message}"
                    }
                } else {
                    dataFetchError = "Could not identify user to load maintenance data."
                    Log.w(logTag, dataFetchError!!)
                }
            } else {
                dataFetchError = "No vehicle selected to log maintenance for."
                Log.w(logTag, dataFetchError!!)
            }

            _uiState.update {
                it.copy(
                    currentVehicleId = vehicleId,
                    currentVehicleSeries = vehicleSeriesName,
                    availableMaintenanceTypes = mainMaintenanceTypesWithPlaceholder, // Folosește lista cu placeholder
                    maintenanceItems = if (it.maintenanceItems.isEmpty() && dataFetchError == null) {
                        listOf(createNewUiMaintenanceItem())
                    } else {
                        it.maintenanceItems
                    },
                    isLoading = false,
                    error = dataFetchError ?: it.error
                )
            }
        }
    }

    private fun processRemindersAndPopulateTaskMap(reminders: List<ReminderResponseDto>) {
        val taskMap = mutableMapOf<Int, MutableList<MaintenanceTask>>()
        reminders.forEach { reminder ->
            if (reminder.name.isNotBlank()) {
                taskMap.getOrPut(reminder.typeId) { mutableListOf() }
                    .add(MaintenanceTask(name = reminder.name, isCustomOption = false))
            }
        }
        processedTasksByTypeIdMap = taskMap.mapValues { entry ->
            val taskList = entry.value.distinctBy { it.name }.sortedBy { it.name }.toMutableList()
            taskList.add(0, MaintenanceTask(CUSTOM_TASK_NAME_OPTION, isCustomOption = true))
            taskList.toList()
        }
        Log.d(logTag, "Processed task map from reminders.")
    }

    fun getTasksForType(typeId: Int?): List<MaintenanceTask> {
        if (typeId == null || typeId == 0) {
            return listOf(MaintenanceTask("Select task or go custom...", isCustomOption = false), MaintenanceTask(CUSTOM_TASK_NAME_OPTION, isCustomOption = true))
        }
        val specificTasks = processedTasksByTypeIdMap[typeId]?.toMutableList() ?: mutableListOf()
        if (specificTasks.none { it.isCustomOption }) { // Adaugă opțiunea Custom dacă nu există deja
            specificTasks.add(0, MaintenanceTask(CUSTOM_TASK_NAME_OPTION, isCustomOption = true))
        }
        if (specificTasks.size == 1 && specificTasks.first().isCustomOption) { // Dacă sunt doar Custom, adaugă și un placeholder
            specificTasks.add(0, MaintenanceTask("No specific tasks found...", isCustomOption = false))
        } else if (specificTasks.size > 1 && !specificTasks.any{ it.name == "Select task or go custom..." && !it.isCustomOption}) {
            specificTasks.add(0, MaintenanceTask("Select task or go custom...", isCustomOption = false))
        }
        return specificTasks.distinctBy { it.name } // Asigură unicitatea
    }


    fun onDateChange(newDate: String) {
        val isValidDate = try { LocalDate.parse(newDate, DateTimeFormatter.ISO_LOCAL_DATE); true } catch (e: Exception) { false }
        _uiState.update { it.copy(date = newDate, dateError = if (!isValidDate && newDate.isNotBlank()) "Invalid date (YYYY-MM-DD)" else null) }
    }

    fun onMileageChange(newMileage: String) {
        val mileageValue = newMileage.filter { it.isDigit() }
        val longValue = mileageValue.toLongOrNull()
        val error = if (mileageValue.isNotBlank() && longValue == null) "Mileage must be a number"
        else if (longValue != null && longValue <= 0) "Mileage must be > 0"
        else null
        _uiState.update { it.copy(mileage = mileageValue, mileageError = error) }
    }

    fun onServiceProviderChange(newProvider: String) { _uiState.update { it.copy(serviceProvider = newProvider) } }
    fun onNotesChange(newNotes: String) { _uiState.update { it.copy(notes = newNotes) } }

    fun onCostChange(newCost: String) {
        val costValue = newCost.filter { it.isDigit() || it == '.' }
        val doubleValue = costValue.toDoubleOrNull()
        val error = if (costValue.isNotBlank() && costValue != "." && doubleValue == null) "Invalid cost amount"
        else if (doubleValue != null && doubleValue < 0) "Cost cannot be negative"
        else null
        _uiState.update { it.copy(cost = costValue, costError = error) }
    }

    fun onMaintenanceTypeSelected(itemUniqueId: String, selectedType: MaintenanceType?) {
        _uiState.update { currentState ->
            val updatedItems = currentState.maintenanceItems.map { item ->
                if (item.id == itemUniqueId) {
                    val newTypeId = if (selectedType?.id == 0) null else selectedType?.id
                    item.copy(
                        selectedMaintenanceTypeId = newTypeId,
                        selectedTaskName = null, // Resetează task-ul
                        customTaskNameInput = "",
                        showCustomTaskNameInput = newTypeId == null // Nu arăta custom input dacă niciun tip nu e selectat
                    )
                } else item
            }
            val newItemErrors = currentState.itemErrors.toMutableMap().apply { remove(itemUniqueId) }
            currentState.copy(maintenanceItems = updatedItems, itemErrors = newItemErrors)
        }
    }

    fun onMaintenanceTaskSelected(itemUniqueId: String, selectedTask: MaintenanceTask?) {
        _uiState.update { currentState ->
            val updatedItems = currentState.maintenanceItems.map { item ->
                if (item.id == itemUniqueId) {
                    val isNowCustom = selectedTask?.isCustomOption == true
                    // Dacă selectează placeholder-ul "Select task..." sau "No specific tasks...", considerăm că nu e o selecție validă încă
                    val actualSelectedTaskName = if (selectedTask?.name == "Select task or go custom..." || selectedTask?.name == "No specific tasks found...") null else selectedTask?.name

                    item.copy(
                        selectedTaskName = if (isNowCustom) CUSTOM_TASK_NAME_OPTION else actualSelectedTaskName,
                        showCustomTaskNameInput = isNowCustom,
                        customTaskNameInput = if (isNowCustom) item.customTaskNameInput else ""
                    )
                } else item
            }
            val newItemErrors = currentState.itemErrors.toMutableMap().apply { remove(itemUniqueId) }
            currentState.copy(maintenanceItems = updatedItems, itemErrors = newItemErrors)
        }
    }

    fun onCustomTaskNameChanged(itemUniqueId: String, customName: String) {
        _uiState.update { currentState ->
            val updatedItems = currentState.maintenanceItems.map { item ->
                if (item.id == itemUniqueId && item.showCustomTaskNameInput) {
                    item.copy(customTaskNameInput = customName, selectedTaskName = customName.trim().ifBlank { CUSTOM_TASK_NAME_OPTION })
                } else item
            }
            val newItemErrors = currentState.itemErrors.toMutableMap()
            if (customName.isNotBlank() && customName != CUSTOM_TASK_NAME_OPTION) newItemErrors.remove(itemUniqueId)
            currentState.copy(maintenanceItems = updatedItems, itemErrors = newItemErrors)
        }
    }

    fun addMaintenanceItem() {
        _uiState.update {
            it.copy(maintenanceItems = it.maintenanceItems + createNewUiMaintenanceItem())
        }
    }

    fun removeMaintenanceItem(itemUniqueId: String) {
        _uiState.update { currentState ->
            val updatedItems = currentState.maintenanceItems.filterNot { item -> item.id == itemUniqueId }
            val updatedErrors = currentState.itemErrors.toMutableMap().also { it.remove(itemUniqueId) }
            currentState.copy(
                maintenanceItems = if (updatedItems.isEmpty()) listOf(createNewUiMaintenanceItem()) else updatedItems,
                itemErrors = updatedErrors
            )
        }
    }

    fun saveMaintenance() {
        val currentState = _uiState.value
        var newItemErrors = mutableMapOf<String, String?>()
        _uiState.update { it.copy(mileageError = null, dateError = null, costError = null, error = null, itemErrors = emptyMap()) }
        var hasGlobalErrors = false

        if (currentState.currentVehicleId == null) { _uiState.update { it.copy(error = "No vehicle selected.") }; return }
        if (currentState.mileage.isBlank() || currentState.mileage.toDoubleOrNull() == null || currentState.mileage.toDouble() <= 0) {
            _uiState.update { it.copy(mileageError = "Valid mileage (> 0) is required.") }; hasGlobalErrors = true
        }
        try { LocalDate.parse(currentState.date, DateTimeFormatter.ISO_LOCAL_DATE) }
        catch (e: Exception) { _uiState.update { it.copy(dateError = "Valid date is required (YYYY-MM-DD).") }; hasGlobalErrors = true }

        val costDouble = currentState.cost.toDoubleOrNull()
        if (currentState.cost.isNotBlank() && (costDouble == null || costDouble < 0)) {
            _uiState.update { it.copy(costError = "If entered, cost must be a valid non-negative number.")}; hasGlobalErrors = true
        }

        val itemsToSave = mutableListOf<MaintenanceItemDto>()
        var hasItemErrors = false
        currentState.maintenanceItems.forEach { uiItem ->
            if (uiItem.selectedMaintenanceTypeId == null || uiItem.selectedMaintenanceTypeId == 0) {
                newItemErrors[uiItem.id] = "Category is required."
                hasItemErrors = true
            } else {
                val finalTaskName = if (uiItem.showCustomTaskNameInput) {
                    uiItem.customTaskNameInput.trim()
                } else {
                    uiItem.selectedTaskName?.takeIf { it != CUSTOM_TASK_NAME_OPTION && it != "Select Task..." && it != "Select task or go custom..." && it != "No specific tasks found..." }
                }

                if (finalTaskName.isNullOrBlank()) {
                    newItemErrors[uiItem.id] = if (uiItem.showCustomTaskNameInput) "Custom task description is required." else "Specific task is required."
                    hasItemErrors = true
                } else {
                    itemsToSave.add(
                        MaintenanceItemDto(
                            typeId = uiItem.selectedMaintenanceTypeId!!,
                            name = finalTaskName
                        )
                    )
                }
            }
        }
        _uiState.update { it.copy(itemErrors = newItemErrors) }

        if (itemsToSave.isEmpty() && currentState.maintenanceItems.isNotEmpty()) {
            _uiState.update { it.copy(error = if (hasItemErrors) "Fix errors in tasks." else "Add at least one valid task.") }
            return
        }
        if (currentState.maintenanceItems.isEmpty()){
            _uiState.update { it.copy(error = "Add at least one maintenance task.") }
            return
        }
        if (hasGlobalErrors || hasItemErrors) {
            Log.d(logTag, "Save attempt failed due to validation errors.")
            return
        }

        val request = MaintenanceSaveRequestDto(
            vehicleId = currentState.currentVehicleId!!,
            date = currentState.date,
            mileage = currentState.mileage.toDouble(),
            maintenanceItems = itemsToSave,
            serviceProvider = currentState.serviceProvider.ifBlank { "" },
            notes = currentState.notes.ifBlank { "" },
            cost = costDouble ?: 0.0
        )

        Log.d(logTag, "Saving maintenance: $request")
        _uiState.update { it.copy(isLoading = true, saveSuccess = false) }
        viewModelScope.launch {
            val result = vehicleRepository.saveVehicleMaintenance(request)
            result.onSuccess {
                Log.i(logTag, "Maintenance log saved successfully.")
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            }.onFailure { exception ->
                Log.e(logTag, "Failed to save maintenance log: ${exception.message}", exception)
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Failed to save.") }
            }
        }
    }
    fun resetSaveStatus() { _uiState.update { it.copy(saveSuccess = false, error = null) } }
}