package com.example.cartrack.features.add_maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.MaintenanceItemDto
import com.example.cartrack.core.data.model.maintenance.MaintenanceSaveRequestDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddMaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMaintenanceUiState())
    val uiState: StateFlow<AddMaintenanceUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableStateFlow<AddMaintenanceEvent?>(null)
    val eventFlow: StateFlow<AddMaintenanceEvent?> = _eventFlow.asStateFlow()

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            if (vehicleId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No vehicle selected. Please go back and select a vehicle.") }
                return@launch
            }

            val vehicleNameResult = vehicleRepository.getVehiclesByClientId()
            val remindersResult = vehicleRepository.getRemindersByVehicleId(vehicleId)

            if (remindersResult.isSuccess && vehicleNameResult.isSuccess) {
                val vehicleName = vehicleNameResult.getOrNull()?.find { it.id == vehicleId }?.let { "${it.producer} ${it.series}" } ?: "Vehicle"
                val reminders = remindersResult.getOrNull() ?: emptyList()

                val selectable = reminders
                    .filter { it.isActive || it.statusId > 1 }
                    .sortedBy { it.name }
                    .map { SelectableReminder(it, isSelected = it.statusId > 1) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentVehicleId = vehicleId,
                        currentVehicleSeries = vehicleName,
                        selectableReminders = selectable
                    )
                }
            } else {
                val errorMsg = remindersResult.exceptionOrNull()?.message ?: vehicleNameResult.exceptionOrNull()?.message ?: "Failed to load initial data."
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    fun onDateChange(newDate: String) { _uiState.update { it.copy(date = newDate, mileageError = null) } }
    fun onMileageChange(newMileage: String) { _uiState.update { it.copy(mileage = newMileage.filter { c -> c.isDigit() }, mileageError = null) } }
    fun onServiceProviderChange(provider: String) { _uiState.update { it.copy(serviceProvider = provider) } }
    fun onNotesChange(newNotes: String) { _uiState.update { it.copy(notes = newNotes) } }
    fun onCostChange(newCost: String) { _uiState.update { it.copy(cost = newCost.filter { c -> c.isDigit() || c == '.' }) } }

    fun onReminderToggled(configId: Int, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedReminders = state.selectableReminders.map {
                if (it.reminder.configId == configId) it.copy(isSelected = isSelected) else it
            }
            state.copy(selectableReminders = updatedReminders, tasksError = null)
        }
    }

    fun onCustomTaskChanged(id: String, name: String) {
        _uiState.update { state ->
            val updatedTasks = state.customTasks.map {
                if (it.id == id) it.copy(name = name) else it
            }
            state.copy(customTasks = updatedTasks, tasksError = null)
        }
    }

    fun addCustomTaskField() {
        _uiState.update { it.copy(customTasks = it.customTasks + CustomTask()) }
    }

    fun removeCustomTaskField(id: String) {
        _uiState.update { state ->
            if (state.customTasks.size > 1) {
                state.copy(customTasks = state.customTasks.filterNot { it.id == id })
            } else {
                state.copy(customTasks = listOf(CustomTask(name = "")))
            }
        }
    }

    fun eventConsumed() {
        _eventFlow.value = null
    }

    fun saveMaintenance() {
        val state = _uiState.value
        if (state.mileage.isBlank() || state.mileage.toDoubleOrNull() == null) {
            _uiState.update { it.copy(mileageError = "Mileage is required and must be a valid number.") }
            return
        }

        val selectedReminderItems = state.selectableReminders.filter { it.isSelected }.map { MaintenanceItemDto(configId = it.reminder.configId, customName = null) }
        val customTaskItems = state.customTasks.filter { it.name.isNotBlank() }.map { MaintenanceItemDto(configId = null, customName = it.name.trim()) }
        val allItems = selectedReminderItems + customTaskItems

        if (allItems.isEmpty()) {
            _uiState.update { it.copy(tasksError = "Please select at least one task or add a custom one.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, mileageError = null, tasksError = null) }

        val request = MaintenanceSaveRequestDto(
            vehicleId = state.currentVehicleId!!,
            date = state.date,
            mileage = state.mileage.toDouble(),
            maintenanceItems = allItems,
            serviceProvider = state.serviceProvider.trim(),
            notes = state.notes.trim(),
            cost = state.cost.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            val result = vehicleRepository.saveVehicleMaintenance(request)

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _eventFlow.value = AddMaintenanceEvent.ShowToast("Maintenance log saved successfully!")
                _eventFlow.value = AddMaintenanceEvent.NavigateBack
            }.onFailure { e ->
                val errorMessage = try {
                    (e as? ClientRequestException)?.response?.bodyAsText() ?: e.message
                } catch (_: Exception) {
                    e.message
                } ?: "An unknown error occurred."

                if (e is ClientRequestException && e.response.status.value == 400) {
                    _uiState.update { it.copy(isSaving = false, mileageError = errorMessage) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = errorMessage) }
                    _eventFlow.value = AddMaintenanceEvent.ShowToast("Error: $errorMessage")
                }
            }
        }
    }
}