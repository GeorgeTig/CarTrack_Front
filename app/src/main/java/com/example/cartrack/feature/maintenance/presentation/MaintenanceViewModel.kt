package com.example.cartrack.feature.maintenance.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderRequestDto
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleCacheManager: VehicleManager // Using your VehicleManager interface
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<MaintenanceEvent>()
    val eventFlow: SharedFlow<MaintenanceEvent> = _eventFlow.asSharedFlow()

    private val logTag = "MaintenanceVM"

    init {
        observeSelectedVehicle()
    }

    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow // Assuming this is StateFlow<Int?>
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to $vehicleId.")
                    // Reset entire state for the new vehicle ID, except the ID itself
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId)
                    if (vehicleId != null) {
                        fetchReminders(vehicleId)
                    }
                }
        }
    }

    fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        if (!isRetry || _uiState.value.error == null) { // Show loading unless just clearing error
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(error = null) } // Just clear error on retry
        }
        Log.d(logTag, "Fetching reminders for vehicle ID: $vehicleId")

        viewModelScope.launch {
            val result = vehicleRepository.getRemindersByVehicleId(vehicleId)
            result.onSuccess { data ->
                Log.d(logTag, "Fetched ${data.size} reminders.")
                val types = data
                    .filter { it.typeName.isNotBlank() }
                    .distinctBy { it.typeId }
                    .map { TypeFilterItem(it.typeId, it.typeName, MaintenanceTypeIcon.fromTypeId(it.typeId)) }
                    .sortedBy { it.name }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        reminders = data,
                        availableTypes = types,
                        filteredReminders = applyFilters(
                            data,
                            currentState.searchQuery,
                            currentState.selectedFilterTab,
                            currentState.selectedTypeId
                        )
                    )
                }
            }
            result.onFailure { e ->
                Log.e(logTag, "Failed to fetch reminders: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load reminders.") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentUiState = _uiState.value
        var newFilterType = currentUiState.selectedFilterTab
        var newTypeId: Int? = currentUiState.selectedTypeId
        var effectiveQuery = query

        val typeTagMatch = Regex("^#([\\w\\s]+)$").find(query.trim())
        if (typeTagMatch != null) {
            val typeNameFromTag = typeTagMatch.groupValues[1].trim()
            val matchedType = currentUiState.availableTypes.find { it.name.equals(typeNameFromTag, ignoreCase = true) }
            if (matchedType != null) {
                newTypeId = matchedType.id; newFilterType = MaintenanceFilterType.TYPE; effectiveQuery = ""
            } else {
                newFilterType = MaintenanceFilterType.ALL; newTypeId = null
            }
        } else if (query.isBlank()) {
            if (newFilterType == MaintenanceFilterType.TYPE) newTypeId = null // Keep TYPE tab, clear specific type
        } else { // Regular text typed
            if (newFilterType == MaintenanceFilterType.TYPE) { newFilterType = MaintenanceFilterType.ALL; newTypeId = null }
        }

        _uiState.update {
            it.copy(
                searchQuery = query, // Store raw query
                selectedFilterTab = newFilterType,
                selectedTypeId = newTypeId,
                filteredReminders = applyFilters(it.reminders, effectiveQuery, newFilterType, newTypeId)
            )
        }
    }

    fun selectFilterTab(filterType: MaintenanceFilterType) {
        // Allow re-clicking TYPE tab to potentially clear specific type filter if no search query exists
        if (_uiState.value.selectedFilterTab == filterType &&
            (filterType != MaintenanceFilterType.TYPE || _uiState.value.searchQuery.startsWith("#"))) {
            return
        }

        Log.d(logTag, "Filter tab selected: $filterType")
        _uiState.update {
            it.copy(
                selectedFilterTab = filterType,
                selectedTypeId = if (filterType == MaintenanceFilterType.TYPE) it.selectedTypeId else null, // Preserve selected type if TYPE tab
                searchQuery = "", // Clear search query when changing main tabs
                filteredReminders = applyFilters(
                    it.reminders, "", filterType,
                    if (filterType == MaintenanceFilterType.TYPE) it.selectedTypeId else null
                )
            )
        }
    }

    fun selectTypeFilter(typeId: Int) {
        val type = _uiState.value.availableTypes.find { it.id == typeId } ?: return
        Log.d(logTag, "Specific Type selected: ${type.name} (ID: $typeId)")

        if (_uiState.value.selectedFilterTab == MaintenanceFilterType.TYPE && _uiState.value.selectedTypeId == typeId) return

        _uiState.update {
            it.copy(
                selectedFilterTab = MaintenanceFilterType.TYPE,
                selectedTypeId = typeId,
                searchQuery = "#${type.name}", // Reflect selection in search bar
                filteredReminders = applyFilters(it.reminders, "", MaintenanceFilterType.TYPE, typeId)
            )
        }
    }

    private fun applyFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        filterType: MaintenanceFilterType,
        typeId: Int?
    ): List<ReminderResponseDto> {
        val queryFiltered = if (query.isNotBlank() && !query.startsWith("#")) {
            reminders.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.typeName.contains(query, ignoreCase = true)
            }
        } else { reminders }

        return when (filterType) {
            MaintenanceFilterType.ALL -> queryFiltered
            MaintenanceFilterType.WARNINGS -> queryFiltered.filter { setOf(2, 3).contains(it.statusId) } // 2=DueSoon, 3=Overdue
            MaintenanceFilterType.TYPE -> if (typeId != null) reminders.filter { it.typeId == typeId } else queryFiltered
        }
    }

    fun refreshReminders() {
        Log.d(logTag,"Refresh reminders requested.")
        _uiState.value.selectedVehicleId?.let { fetchReminders(it, isRetry = true) }
            ?: Log.w(logTag, "Refresh requested but no vehicle is selected.")
    }

    fun showReminderDetails(reminder: ReminderResponseDto) {
        _uiState.update { it.copy(reminderForDetailView = reminder, isEditDialogVisible = false) }
    }

    fun dismissReminderDetails() {
        _uiState.update { it.copy(reminderForDetailView = null) }
    }

    fun showEditReminderDialog() {
        val reminderToEdit = _uiState.value.reminderForDetailView ?: return Unit.also {
            Log.e(logTag, "showEditReminderDialog: No reminderForDetailView set.")
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("Cannot edit: No reminder selected.")) }
        }
        _uiState.update {
            it.copy(
                reminderForDetailView = null, isEditDialogVisible = true,
                editFormState = EditReminderFormState(
                    reminderToEdit = reminderToEdit, nameInput = reminderToEdit.name,
                    mileageIntervalInput = reminderToEdit.mileageInterval?.toString() ?: "",
                    timeIntervalInput = reminderToEdit.timeInterval.toString()
                )
            )
        }
    }

    fun dismissEditReminderDialog() {
        _uiState.update { it.copy(isEditDialogVisible = false, editFormState = EditReminderFormState()) }
    }

    fun onEditNameChanged(name: String) {
        _uiState.update { it.copy(editFormState = it.editFormState.copy(nameInput = name, nameError = if (name.isBlank()) "Name cannot be empty" else null)) }
    }

    fun onEditMileageIntervalChanged(mileage: String) {
        val digits = mileage.filter { it.isDigit() }
        val isValid = digits.isEmpty() || (digits.toIntOrNull()?.let { it >= 0 } ?: false)
        _uiState.update { it.copy(editFormState = it.editFormState.copy(mileageIntervalInput = digits, mileageIntervalError = if (!isValid) "Invalid mileage (>= 0 or empty)" else null)) }
    }

    fun onEditTimeIntervalChanged(time: String) {
        val digits = time.filter { it.isDigit() }
        val isValid = digits.toIntOrNull()?.let { it > 0 } ?: false
        _uiState.update { it.copy(editFormState = it.editFormState.copy(timeIntervalInput = digits, timeIntervalError = if (!isValid) "Time interval must be > 0 days" else null)) }
    }

    fun saveReminderEdits() {
        val form = _uiState.value.editFormState
        val originalReminder = form.reminderToEdit ?: return Unit.also {
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("Error saving: Original reminder data missing.")) }
        }

        var nameErr: String? = if (form.nameInput.isBlank()) "Name cannot be empty" else null
        var mileageErr: String? = null
        var timeErr: String? = null
        var hasError = nameErr != null

        val mileageString = form.mileageIntervalInput.trim()
        val parsedMileageInterval: Int
        if (mileageString.isEmpty()) {
            parsedMileageInterval = 0
        } else {
            parsedMileageInterval = mileageString.toInt()
            if (parsedMileageInterval < 0) {
                mileageErr = "Mileage must be a valid number (>= 0) or empty"
                hasError = true
            }
        }

        val timeString = form.timeIntervalInput.trim()
        val parsedTimeInterval: Int?
        if (timeString.isBlank()) {
            timeErr = "Time interval is required (must be > 0)"
            hasError = true
            parsedTimeInterval = null
        } else {
            parsedTimeInterval = timeString.toIntOrNull()
            if (parsedTimeInterval == null || parsedTimeInterval < 0) {
                timeErr = "Time interval must be a valid number => 0"
                hasError = true
            }
        }

        _uiState.update { it.copy(editFormState = form.copy(nameError = nameErr, mileageIntervalError = mileageErr, timeIntervalError = timeErr)) }

        if (hasError) {
            Log.w(logTag, "Save edits with validation errors.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        // Assuming ReminderRequestDto expects name, mileageInterval (nullable), and timeInterval (non-nullable)
        val request = ReminderRequestDto(
            id = originalReminder.configId,
            // name = form.nameInput.trim(), // Uncomment if your DTO and backend handle name update
            mileageInterval = parsedMileageInterval,
            timeInterval = parsedTimeInterval!! // Safe due to validation
        )

        Log.d(logTag, "Attempting to update reminder: $request")
        viewModelScope.launch {
            val result = vehicleRepository.updateReminder(request)
            result.onSuccess {
                _eventFlow.emit(MaintenanceEvent.ShowMessage("Reminder updated!"))
                dismissEditReminderDialog()
                _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
            }
            result.onFailure { e -> _eventFlow.emit(MaintenanceEvent.ShowError(e.message ?: "Failed to save changes.")) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun restoreReminderToDefaults() {
        val reminderToRestore = _uiState.value.editFormState.reminderToEdit ?: return Unit.also {
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("Error restoring: Original data missing.")) }
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderToDefault(reminderToRestore.configId)
            result.onSuccess {
                _eventFlow.emit(MaintenanceEvent.ShowMessage("Restored to defaults!"))
                dismissEditReminderDialog()
                _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
            }
            result.onFailure { e -> _eventFlow.emit(MaintenanceEvent.ShowError(e.message ?: "Failed to restore.")) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleReminderActiveStatus() {
        val reminderToToggle = _uiState.value.reminderForDetailView ?: return Unit.also {
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("No reminder selected.")) }
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderActiveStatus(reminderToToggle.configId)
            result.onSuccess {
                _eventFlow.emit(MaintenanceEvent.ShowMessage("Status updated!"))
                _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
                dismissReminderDetails()
            }
            result.onFailure { e -> _eventFlow.emit(MaintenanceEvent.ShowError(e.message ?: "Failed to update status.")) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}