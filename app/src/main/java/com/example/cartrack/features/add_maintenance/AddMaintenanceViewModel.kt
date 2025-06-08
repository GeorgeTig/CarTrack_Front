package com.example.cartrack.features.add_maintenance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.MaintenanceItemDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class AddMaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMaintenanceUiState())
    val uiState: StateFlow<AddMaintenanceUiState> = _uiState.asStateFlow()

    private var tasksByTypeIdMap: Map<Int, List<MaintenanceTask>> = emptyMap()
    private val logTag = "AddMaintenanceVM"

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            if (vehicleId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No vehicle selected. Please go back and select a vehicle from the Home screen.") }
                return@launch
            }

            // Obține numele vehiculului
            val vehicleName = vehicleRepository.getVehiclesByClientId()
                .getOrNull()?.find { it.id == vehicleId }?.series ?: "Vehicle"

            // Obține reminderele pentru a construi opțiunile
            vehicleRepository.getRemindersByVehicleId(vehicleId).onSuccess { reminders ->
                processRemindersToBuildOptions(reminders)
                val maintenanceTypes = reminders
                    .map { MaintenanceType(it.typeId, it.typeName) }
                    .distinctBy { it.id }
                    .sortedBy { it.name }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentVehicleId = vehicleId,
                        currentVehicleSeries = vehicleName,
                        availableMaintenanceTypes = maintenanceTypes,
                        maintenanceItems = listOf(UiMaintenanceItem()) // Adaugă un prim item gol
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun processRemindersToBuildOptions(reminders: List<ReminderResponseDto>) {
        tasksByTypeIdMap = reminders
            .groupBy { it.typeId }
            .mapValues { entry ->
                entry.value.map { MaintenanceTask(it.name) }.distinct().sortedBy { it.name }
            }
    }

    fun getTasksForType(typeId: Int?): List<MaintenanceTask> {
        val tasks = tasksByTypeIdMap[typeId]?.toMutableList() ?: mutableListOf()
        tasks.add(MaintenanceTask(CUSTOM_TASK_NAME_OPTION, isCustomOption = true)) // Adaugă opțiunea custom
        return tasks
    }

    // --- Handlers pentru input-uri ---
    fun onDateChange(newDate: String) { _uiState.update { it.copy(date = newDate, dateError = null) } }
    fun onMileageChange(mileage: String) { _uiState.update { it.copy(mileage = mileage.filter { it.isDigit() }, mileageError = null) } }
    fun onServiceProviderChange(provider: String) { _uiState.update { it.copy(serviceProvider = provider) } }
    fun onNotesChange(notes: String) { _uiState.update { it.copy(notes = notes) } }
    fun onCostChange(cost: String) { _uiState.update { it.copy(cost = cost.filter { c -> c.isDigit() || c == '.' }, costError = null) } }
    fun addMaintenanceItem() { _uiState.update { it.copy(maintenanceItems = it.maintenanceItems + UiMaintenanceItem()) } }
    fun removeMaintenanceItem(id: String) { _uiState.update { it.copy(maintenanceItems = it.maintenanceItems.filterNot { item -> item.id == id }) } }

    fun onMaintenanceTypeSelected(itemId: String, type: MaintenanceType?) {
        _uiState.update { state ->
            val updatedItems = state.maintenanceItems.map { item ->
                if (item.id == itemId) item.copy(selectedMaintenanceTypeId = type?.id, selectedTaskName = null, showCustomTaskNameInput = false, customTaskNameInput = "")
                else item
            }
            val newErrors = state.itemErrors.toMutableMap().apply { remove(itemId) }
            state.copy(maintenanceItems = updatedItems, itemErrors = newErrors)
        }
    }

    fun onMaintenanceTaskSelected(itemId: String, task: MaintenanceTask?) {
        _uiState.update { state ->
            val updatedItems = state.maintenanceItems.map { item ->
                if (item.id == itemId) {
                    val isCustom = task?.isCustomOption == true
                    item.copy(selectedTaskName = if (isCustom) null else task?.name, showCustomTaskNameInput = isCustom)
                } else item
            }
            val newErrors = state.itemErrors.toMutableMap().apply { remove(itemId) }
            state.copy(maintenanceItems = updatedItems, itemErrors = newErrors)
        }
    }

    fun onCustomTaskNameChanged(itemId: String, name: String) {
        _uiState.update { state ->
            val updatedItems = state.maintenanceItems.map { item ->
                if (item.id == itemId) item.copy(customTaskNameInput = name) else item
            }
            val newErrors = state.itemErrors.toMutableMap().apply { remove(itemId) }
            state.copy(maintenanceItems = updatedItems, itemErrors = newErrors)
        }
    }

    fun saveMaintenance() {
        if (!validateForm()) return

        _uiState.update { it.copy(isSaving = true, error = null) }
        val state = _uiState.value

        val request = MaintenanceSaveRequestDto(
            vehicleId = state.currentVehicleId!!,
            date = state.date,
            mileage = state.mileage.toDouble(),
            maintenanceItems = state.maintenanceItems.mapNotNull { item ->
                item.selectedMaintenanceTypeId?.let { typeId ->
                    val taskName = if (item.showCustomTaskNameInput) item.customTaskNameInput.trim() else item.selectedTaskName
                    taskName?.let { MaintenanceItemDto(typeId, it) }
                }
            },
            serviceProvider = state.serviceProvider.trim(),
            notes = state.notes.trim(),
            cost = state.cost.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            vehicleRepository.saveVehicleMaintenance(request).onSuccess {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true
        // Curăță erorile vechi
        _uiState.update { it.copy(dateError = null, mileageError = null, costError = null, itemErrors = emptyMap(), error = null) }

        try { LocalDate.parse(state.date) } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(dateError = "Invalid date format.") }; isValid = false
        }
        if (state.mileage.isBlank() || state.mileage.toDoubleOrNull() == null) {
            _uiState.update { it.copy(mileageError = "Mileage is required.") }; isValid = false
        }
        if (state.cost.isNotEmpty() && state.cost.toDoubleOrNull() == null) {
            _uiState.update { it.copy(costError = "If provided, cost must be a valid number.") }; isValid = false
        }

        val itemErrors = mutableMapOf<String, String>()
        state.maintenanceItems.forEach { item ->
            if (item.selectedMaintenanceTypeId == null) {
                itemErrors[item.id] = "Please select a maintenance type."
                isValid = false
            } else if (!item.showCustomTaskNameInput && item.selectedTaskName.isNullOrBlank()) {
                itemErrors[item.id] = "Please select a specific task."
                isValid = false
            } else if (item.showCustomTaskNameInput && item.customTaskNameInput.isBlank()) {
                itemErrors[item.id] = "Please describe the custom task."
                isValid = false
            }
        }

        if (itemErrors.isNotEmpty()) {
            _uiState.update { it.copy(itemErrors = itemErrors) }
        }

        if (state.maintenanceItems.isEmpty() || !state.maintenanceItems.any { it.selectedMaintenanceTypeId != null }) {
            _uiState.update { it.copy(error = "Please add at least one valid maintenance task.") }
            isValid = false
        }

        return isValid
    }

    fun resetSaveStatus() { _uiState.update { it.copy(saveSuccess = false, error = null) } }
}