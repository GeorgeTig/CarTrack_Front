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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

@HiltViewModel
class AddMaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMaintenanceUiState())
    val uiState: StateFlow<AddMaintenanceUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddMaintenanceEvent>()
    val eventFlow: SharedFlow<AddMaintenanceEvent> = _eventFlow.asSharedFlow()

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
            val vehicleInfoDeferred = async { vehicleRepository.getVehicleInfo(vehicleId) }
            val vehicleNameDeferred = async { vehicleRepository.getVehiclesByClientId() }
            val remindersDeferred = async { vehicleRepository.getRemindersByVehicleId(vehicleId) }
            val typesDeferred = async { vehicleRepository.getAllReminderTypes() }

            val vehicleInfoResult = vehicleInfoDeferred.await()
            val vehicleNameResult = vehicleNameDeferred.await()
            val remindersResult = remindersDeferred.await()
            val typesResult = typesDeferred.await()

            if (vehicleNameResult.isFailure || remindersResult.isFailure || typesResult.isFailure || vehicleInfoResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load initial data.") }
                return@launch
            }

            val vehicleName = vehicleNameResult.getOrNull()?.find { it.id == vehicleId }?.let { "${it.producer} ${it.series}" } ?: "Vehicle"
            val currentMileage = vehicleInfoResult.getOrNull()?.mileage

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentVehicleId = vehicleId,
                    currentVehicleSeries = vehicleName,
                    currentVehicleMileage = currentMileage,
                    availableScheduledTasks = remindersResult.getOrNull()?.filter { r -> r.isActive }?.sortedBy { it.name } ?: emptyList(),
                    availableMaintenanceTypes = typesResult.getOrNull()?.sortedBy { t -> t.name } ?: emptyList()
                )
            }
        }
    }

    fun onDateChange(newDate: String) { _uiState.update { it.copy(date = newDate) } }

    fun onMileageChange(newMileage: String) {
        _uiState.update { it.copy(mileage = newMileage.filter(Char::isDigit).take(7), mileageError = null) }
    }

    fun onServiceProviderChange(provider: String) { _uiState.update { it.copy(serviceProvider = provider) } }
    fun onNotesChange(newNotes: String) { _uiState.update { it.copy(notes = newNotes) } }
    fun onCostChange(newCost: String) { _uiState.update { it.copy(cost = newCost.filter { it.isDigit() || it == '.' }) } }

    fun addScheduledTask() { _uiState.update { it.copy(logEntries = it.logEntries + LogEntryItem.Scheduled()) } }
    fun addCustomTask() { _uiState.update { it.copy(logEntries = it.logEntries + LogEntryItem.Custom()) } }
    fun removeLogEntry(id: String) { _uiState.update { state -> state.copy(logEntries = state.logEntries.filterNot { it.id == id }) } }

    fun onScheduledEntryTypeChanged(entryId: String, typeId: Int) {
        updateEntry(entryId) { (it as? LogEntryItem.Scheduled)?.copy(selectedTypeId = typeId, selectedReminderId = null) ?: it }
    }

    fun onScheduledTaskSelected(entryId: String, reminderId: Int) {
        updateEntry(entryId) { (it as? LogEntryItem.Scheduled)?.copy(selectedReminderId = reminderId) ?: it }
    }

    fun onCustomTaskNameChanged(entryId: String, name: String) {
        updateEntry(entryId) { (it as? LogEntryItem.Custom)?.copy(name = name) ?: it }
    }

    private fun updateEntry(entryId: String, transform: (LogEntryItem) -> LogEntryItem) {
        _uiState.update { currentState ->
            val newList = currentState.logEntries.map { if (it.id == entryId) transform(it) else it }
            currentState.copy(logEntries = newList)
        }
    }

    fun saveMaintenance() {
        val state = _uiState.value
        val newMileage = state.mileage.toDoubleOrNull()

        if (newMileage == null) {
            _uiState.update { it.copy(mileageError = "Mileage is required.") }
            return
        }

        state.currentVehicleMileage?.let { currentMileage ->
            if (newMileage > currentMileage) {
                _uiState.update { it.copy(mileageError = "Cannot be more than current mileage (${currentMileage.toInt()} km).") }
                return
            }
        }

        val maintenanceItems = state.logEntries.mapNotNull { entry ->
            when (entry) {
                is LogEntryItem.Scheduled -> entry.selectedReminderId?.let { MaintenanceItemDto(configId = it, customName = null) }
                is LogEntryItem.Custom -> if (entry.name.isNotBlank()) MaintenanceItemDto(configId = null, customName = entry.name.trim()) else null
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
            mileage = newMileage,
            maintenanceItems = maintenanceItems,
            serviceProvider = state.serviceProvider.trim(),
            notes = state.notes.trim(),
            cost = state.cost.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            val result = vehicleRepository.saveVehicleMaintenance(request)
            result.onSuccess {
                _eventFlow.emit(AddMaintenanceEvent.ShowToast("Maintenance log saved successfully!"))
                _eventFlow.emit(AddMaintenanceEvent.NavigateBackOnSuccess)
            }.onFailure { e ->
                val errorMessage = if (e is ClientRequestException) {
                    try {
                        val errorBody = e.response.bodyAsText()
                        val jsonObject = Json.decodeFromString<JsonObject>(errorBody)
                        jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: e.message
                    } catch (jsonException: Exception) {
                        e.message
                    }
                } else {
                    e.message ?: "An unknown error occurred."
                }
                _eventFlow.emit(AddMaintenanceEvent.ShowToast("Error: $errorMessage"))
            }
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}