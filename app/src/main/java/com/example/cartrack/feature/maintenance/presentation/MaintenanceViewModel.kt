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

    /** Observes selected vehicle ID from cache and triggers fetching reminders. */
    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to $vehicleId.")
                    val currentMainTab = _uiState.value.selectedMainTab // Preserve current tab
                    // Reset state for the new vehicle ID, keeping the tab
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId, selectedMainTab = currentMainTab)
                    if (vehicleId != null) {
                        fetchReminders(vehicleId)
                    }
                }
        }
    }

    /** Fetches reminders for the given vehicle ID and updates UI state. */
    fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        if (!isRetry || _uiState.value.error == null) {
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
                        filteredReminders = applyAllFilters(
                            data,
                            currentState.searchQuery,
                            currentState.selectedMainTab,
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

    /** Handles changes in the search query input field. */
    fun onSearchQueryChanged(query: String) {
        // Text search is now independent of any #tag logic for types
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredReminders = applyAllFilters(it.reminders, query, it.selectedMainTab, it.selectedTypeId)
            )
        }
    }

    /** Updates state when a main filter tab (Active, Inactive, Warnings) is selected. */
    fun selectMainTab(tab: MaintenanceMainTab) {
        if (_uiState.value.selectedMainTab == tab) return // Avoid redundant processing
        Log.d(logTag, "Main tab selected: $tab")
        _uiState.update {
            it.copy(
                selectedMainTab = tab,
                // Re-apply all filters when main tab changes
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, tab, it.selectedTypeId)
            )
        }
    }

    /** Updates state when a specific type filter (chip) is selected. Null means 'All Types'. */
    fun selectTypeFilter(typeId: Int?) {
        Log.d(logTag, "Type filter selected: ID ${typeId ?: "All"}")
        _uiState.update {
            it.copy(
                selectedTypeId = typeId,
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, it.selectedMainTab, typeId)
            )
        }
    }

    /** Applies all current filters: main tab, type filter, and text search. */
    private fun applyAllFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        mainTab: MaintenanceMainTab,
        typeId: Int?
    ): List<ReminderResponseDto> {
        Log.d(logTag, "Applying all filters: Query='$query', MainTab=$mainTab, TypeId=${typeId ?: "All"}")

        val tabFiltered = when (mainTab) {
            MaintenanceMainTab.ACTIVE -> reminders.filter { it.isActive }
            MaintenanceMainTab.INACTIVE -> reminders.filter { !it.isActive }
            MaintenanceMainTab.WARNINGS -> reminders.filter {
                it.isActive && setOf(2, 3).contains(it.statusId) // Active AND (DueSoon ID=2 or Overdue ID=3)
            }
        }

        val typeFiltered = if (typeId != null) { // typeId == null means "All Types"
            tabFiltered.filter { it.typeId == typeId }
        } else {
            tabFiltered
        }

        val queryFiltered = if (query.isNotBlank()) {
            typeFiltered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.typeName.contains(query, ignoreCase = true)
                // Consider adding status name to search if you have a way to get it from statusId
            }
        } else {
            typeFiltered
        }
        Log.d(logTag, "Filtering resulted in ${queryFiltered.size} reminders.")
        return queryFiltered
    }

    /** Publicly accessible refresh function. */
    fun refreshReminders() {
        Log.d(logTag,"Refresh reminders requested.")
        _uiState.value.selectedVehicleId?.let { fetchReminders(it, isRetry = true) }
            ?: Log.w(logTag, "Refresh requested but no vehicle selected.")
    }

    /** Determines which dialog to show when a reminder item is clicked. */
    fun onReminderItemClicked(reminder: ReminderResponseDto) {
        if (reminder.isActive) {
            _uiState.update { it.copy(reminderForDetailView = reminder, reminderToActivate = null, isEditDialogVisible = false) }
        } else {
            _uiState.update { it.copy(reminderToActivate = reminder, reminderForDetailView = null, isEditDialogVisible = false) }
        }
    }

    /** Hides the detail dialog. */
    fun dismissReminderDetails() {
        _uiState.update { it.copy(reminderForDetailView = null) }
    }

    /** Hides the activation dialog. */
    fun dismissActivateReminderDialog() {
        _uiState.update { it.copy(reminderToActivate = null) }
    }

    /** Toggles the active status of a reminder (used by both detail and activation dialogs). */
    fun toggleReminderActiveStatus(reminderId: Int) {
        _uiState.update { it.copy(isLoading = true) } // General loading for this action
        viewModelScope.launch {
            Log.d(logTag, "Attempting to toggle active status for reminder ID $reminderId")
            val result = vehicleRepository.updateReminderActiveStatus(reminderId)
            result.onSuccess {
                _eventFlow.emit(MaintenanceEvent.ShowMessage("Reminder status updated!"))
                // Dismiss any open dialog and refresh the list
                _uiState.update { it.copy(reminderForDetailView = null, reminderToActivate = null) }
                _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
            }
            result.onFailure { e ->
                _eventFlow.emit(MaintenanceEvent.ShowError(e.message ?: "Failed to update status."))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // --- Edit Dialog Functions ---
    /** Prepares and shows the edit dialog for an active reminder (from detail view). */
    fun showEditReminderDialog() {
        val reminderToEdit = _uiState.value.reminderForDetailView
            ?: return Unit.also {
                Log.e(logTag, "showEditReminderDialog: No reminderForDetailView set for editing.")
                viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("Select an active reminder to edit.")) }
            }
        if (!reminderToEdit.isEditable) { // Check if reminder is editable
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowMessage("This reminder is not editable.")) }
            return
        }

        _uiState.update {
            it.copy(
                reminderForDetailView = null, // Close detail dialog
                reminderToActivate = null,    // Ensure activate dialog is closed
                isEditDialogVisible = true,
                editFormState = EditReminderFormState(
                    reminderToEdit = reminderToEdit, nameInput = reminderToEdit.name,
                    mileageIntervalInput = reminderToEdit.mileageInterval?.toString() ?: "",
                    timeIntervalInput = reminderToEdit.timeInterval.toString()
                )
            )
        }
    }

    /** Hides the edit dialog and resets its form state. */
    fun dismissEditReminderDialog() {
        _uiState.update { it.copy(isEditDialogVisible = false, editFormState = EditReminderFormState()) }
    }

    /** Updates name input in the edit form state and performs validation. */
    fun onEditNameChanged(name: String) {
        _uiState.update {
            it.copy(editFormState = it.editFormState.copy(
                nameInput = name,
                nameError = if (name.isBlank()) "Name cannot be empty" else null
            ))
        }
    }

    /** Updates mileage interval input in the edit form state and performs validation. */
    fun onEditMileageIntervalChanged(mileage: String) {
        val digits = mileage.filter { it.isDigit() }
        val isValid = digits.isEmpty() || (digits.toIntOrNull()?.let { it >= 0 } == true)
        _uiState.update {
            it.copy(editFormState = it.editFormState.copy(
                mileageIntervalInput = digits,
                mileageIntervalError = if (!isValid) "Invalid mileage (>= 0 or empty)" else null
            ))
        }
    }

    /** Updates time interval input in the edit form state and performs validation. */
    fun onEditTimeIntervalChanged(time: String) {
        val digits = time.filter { it.isDigit() }
        val isValid = digits.toIntOrNull()?.let { it > 0 } == true
        _uiState.update {
            it.copy(editFormState = it.editFormState.copy(
                timeIntervalInput = digits,
                timeIntervalError = if (!isValid) "Time interval must be > 0 days" else null
            ))
        }
    }

    /** Validates form inputs and attempts to save the edited reminder details. */
    fun saveReminderEdits() {
        val form = _uiState.value.editFormState
        val originalReminder = form.reminderToEdit ?: return Unit.also {
            viewModelScope.launch { _eventFlow.emit(MaintenanceEvent.ShowError("Error saving: Original reminder data missing.")) }
        }

        var nameErr = if (form.nameInput.isBlank()) "Name cannot be empty" else form.nameError
        var mileageErr = form.mileageIntervalError
        val mileageString = form.mileageIntervalInput.trim()
        val parsedMileageInterval: Int = if (mileageString.isEmpty()) 0 else mileageString.toInt()
        if (mileageString.isNotEmpty() && (parsedMileageInterval < 0)) {
            mileageErr = "Mileage must be a valid number (>= 0) or empty"
        }

        var timeErr = form.timeIntervalError
        val timeString = form.timeIntervalInput.trim()
        val parsedTimeInterval: Int? = if (timeString.isBlank()) null else timeString.toIntOrNull()
        if (parsedTimeInterval == null || parsedTimeInterval <= 0) {
            timeErr = "Time interval is required and must be > 0"
        }

        val hasError = nameErr != null || mileageErr != null || timeErr != null
        _uiState.update { it.copy(editFormState = form.copy(nameError = nameErr, mileageIntervalError = mileageErr, timeIntervalError = timeErr)) }

        if (hasError) {
            Log.w(logTag, "Save edits attempted with validation errors.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        val request = ReminderRequestDto(
            id = originalReminder.configId,

            mileageInterval = parsedMileageInterval,
            timeInterval = parsedTimeInterval!!
        )

        Log.d(logTag, "Attempting to update reminder with request: $request")
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

    /** Attempts to restore the current reminder to its default settings via API. */
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
}