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
import kotlinx.coroutines.async
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
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "No vehicle selected.") }
                return@launch
            }

            val vehicleNameDeferred = async { vehicleRepository.getVehiclesByClientId() }
            val remindersDeferred = async { vehicleRepository.getRemindersByVehicleId(vehicleId) }
            val typesDeferred = async { vehicleRepository.getAllReminderTypes() }

            val vehicleNameResult = vehicleNameDeferred.await()
            val remindersResult = remindersDeferred.await()
            val typesResult = typesDeferred.await()

            if (vehicleNameResult.isFailure || remindersResult.isFailure || typesResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load initial data.") }
                return@launch
            }

            val vehicleName = vehicleNameResult.getOrNull()?.find { it.id == vehicleId }?.let { "${it.producer} ${it.series}" } ?: "Vehicle"

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentVehicleId = vehicleId,
                    currentVehicleSeries = vehicleName,
                    availableScheduledTasks = remindersResult.getOrNull()?.filter { r -> r.isActive }?.sortedBy { it.name } ?: emptyList(),
                    availableMaintenanceTypes = typesResult.getOrNull()?.sortedBy { t -> t.name } ?: emptyList()
                )
            }
        }
    }

    fun onDateChange(newDate: String) { _uiState.update { it.copy(date = newDate) } }
    fun onMileageChange(newMileage: String) { _uiState.update { it.copy(mileage = newMileage.filter(Char::isDigit)) } }
    fun onServiceProviderChange(provider: String) { _uiState.update { it.copy(serviceProvider = provider) } }
    fun onNotesChange(newNotes: String) { _uiState.update { it.copy(notes = newNotes) } }
    fun onCostChange(newCost: String) { _uiState.update { it.copy(cost = newCost.filter { it.isDigit() || it == '.' }) } }
    fun eventConsumed() { _eventFlow.value = null }

    fun addScheduledTask() { _uiState.update { it.copy(logEntries = it.logEntries + LogEntryItem.Scheduled()) } }
    fun addCustomTask() { _uiState.update { it.copy(logEntries = it.logEntries + LogEntryItem.Custom()) } }
    fun removeLogEntry(id: String) { _uiState.update { state -> state.copy(logEntries = state.logEntries.filterNot { it.id == id }) } }

    fun onScheduledEntryTypeChanged(entryId: String, typeId: Int) {
        updateEntry(entryId) { entry ->
            (entry as? LogEntryItem.Scheduled)?.copy(selectedTypeId = typeId, selectedReminderId = null) ?: entry
        }
    }

    fun onScheduledTaskSelected(entryId: String, reminderId: Int) {
        updateEntry(entryId) { entry ->
            (entry as? LogEntryItem.Scheduled)?.copy(selectedReminderId = reminderId) ?: entry
        }
    }

    fun onCustomTaskNameChanged(entryId: String, name: String) {
        updateEntry(entryId) { entry ->
            (entry as? LogEntryItem.Custom)?.copy(name = name) ?: entry
        }
    }

    private fun updateEntry(entryId: String, transform: (LogEntryItem) -> LogEntryItem) {
        _uiState.update { currentState ->
            val newList = currentState.logEntries.map { entry ->
                if (entry.id == entryId) transform(entry) else entry
            }
            currentState.copy(logEntries = newList)
        }
    }

    fun saveMaintenance() {
        val state = _uiState.value
        if (state.mileage.isBlank() || state.mileage.toDoubleOrNull() == null) {
            _uiState.update { it.copy(mileageError = "Mileage is required.") }
            return
        }

        val maintenanceItems = state.logEntries.mapNotNull { entry ->
            when (entry) {
                is LogEntryItem.Scheduled -> {
                    entry.selectedReminderId?.let { MaintenanceItemDto(configId = it, customName = null) }
                }
                is LogEntryItem.Custom -> {
                    // Task-urile custom nu au un tip predefinit, deci trimitem null pentru configId
                    if (entry.name.isNotBlank()) {
                        MaintenanceItemDto(configId = null, customName = entry.name.trim())
                    } else null
                }
            }
        }

        if (maintenanceItems.isEmpty()) {
            _uiState.update { it.copy(entriesError = "Please add at least one maintenance task.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, mileageError = null, entriesError = null) }

        val request = MaintenanceSaveRequestDto(
            vehicleId = state.currentVehicleId!!,
            date = state.date,
            mileage = state.mileage.toDouble(),
            maintenanceItems = maintenanceItems,
            serviceProvider = state.serviceProvider.trim(),
            notes = state.notes.trim(),
            cost = state.cost.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            val result = vehicleRepository.saveVehicleMaintenance(request)
            result.onSuccess {
                _eventFlow.value = AddMaintenanceEvent.ShowToast("Maintenance log saved successfully!")
                _eventFlow.value = AddMaintenanceEvent.NavigateBack
            }.onFailure { e ->
                val errorMessage = (e as? ClientRequestException)?.response?.bodyAsText() ?: e.message ?: "An unknown error occurred."
                _uiState.update { it.copy(error = errorMessage) }
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}