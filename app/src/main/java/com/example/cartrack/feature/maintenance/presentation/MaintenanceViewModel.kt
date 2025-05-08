package com.example.cartrack.feature.maintenance.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleCacheManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val logTag = "MaintenanceVM"

    init {
        observeSelectedVehicle()
    }

    /** Observes selected vehicle ID and triggers fetching reminders on change. */
    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to $vehicleId.")
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId) // Reset state
                    if (vehicleId != null) {
                        fetchReminders(vehicleId)
                    }
                }
        }
    }

    /** Fetches and processes reminders for the given vehicle ID. */
    fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        if (!isRetry || _uiState.value.error == null) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(error = null) }
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
                            reminders = data,
                            query = currentState.searchQuery,
                            filterType = currentState.selectedFilterTab,
                            typeId = currentState.selectedTypeId
                        )
                    )
                }
            }
            result.onFailure { e ->
                Log.e(logTag, "Failed to fetch reminders: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed.") }
            }
        }
    }

    /** Updates state based on search query, handling #Type tags. */
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
                newTypeId = matchedType.id
                newFilterType = MaintenanceFilterType.TYPE
                effectiveQuery = ""
                Log.d(logTag, "Search detected type tag: #${matchedType.name}, ID: $newTypeId")
            } else {
                Log.d(logTag, "Search found type tag '$query' but no matching type.")
                newFilterType = MaintenanceFilterType.ALL
                newTypeId = null
            }
        } else if (query.isBlank()) {
            if (newFilterType == MaintenanceFilterType.TYPE) newTypeId = null else newTypeId = null
        } else {
            if (newFilterType == MaintenanceFilterType.TYPE) {
                newFilterType = MaintenanceFilterType.ALL
                newTypeId = null
            }
        }

        _uiState.update {
            it.copy(
                searchQuery = query,
                selectedFilterTab = newFilterType,
                selectedTypeId = newTypeId,
                filteredReminders = applyFilters(
                    reminders = it.reminders,
                    query = effectiveQuery,
                    filterType = newFilterType,
                    typeId = newTypeId
                )
            )
        }
    }

    /** Updates state when a main filter tab (All, Warnings, Type) is selected. */
    fun selectFilterTab(filterType: MaintenanceFilterType) {
        if (_uiState.value.selectedFilterTab == filterType && filterType != MaintenanceFilterType.TYPE) return

        Log.d(logTag, "Filter tab selected: $filterType")
        _uiState.update {
            it.copy(
                selectedFilterTab = filterType,
                selectedTypeId = if (filterType == MaintenanceFilterType.TYPE) it.selectedTypeId else null,
                searchQuery = if (filterType != MaintenanceFilterType.TYPE || it.selectedFilterTab == MaintenanceFilterType.TYPE) "" else it.searchQuery,
                filteredReminders = applyFilters(
                    reminders = it.reminders,
                    query = "",
                    filterType = filterType,
                    typeId = if (filterType == MaintenanceFilterType.TYPE) it.selectedTypeId else null
                )
            )
        }
    }

    /** Updates state when a specific type filter (e.g., chip) is selected. */
    fun selectTypeFilter(typeId: Int) {
        val type = _uiState.value.availableTypes.find { it.id == typeId } ?: return
        Log.d(logTag, "Specific Type selected: ${type.name} (ID: $typeId)")

        if (_uiState.value.selectedFilterTab == MaintenanceFilterType.TYPE && _uiState.value.selectedTypeId == typeId) return

        _uiState.update {
            it.copy(
                selectedFilterTab = MaintenanceFilterType.TYPE,
                selectedTypeId = typeId,
                searchQuery = "#${type.name}",
                filteredReminders = applyFilters(
                    reminders = it.reminders,
                    query = "",
                    filterType = MaintenanceFilterType.TYPE,
                    typeId = typeId
                )
            )
        }
    }

    /** Applies text, warning, and type filters to the reminder list. */
    private fun applyFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        filterType: MaintenanceFilterType,
        typeId: Int?
    ): List<ReminderResponseDto> {
        Log.d(logTag, "Applying filters: Query='$query', FilterType=$filterType, TypeId=${typeId ?: "N/A"}")

        val queryFiltered = if (query.isNotBlank() && !query.startsWith("#")) {
            reminders.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.typeName.contains(query, ignoreCase = true)
            }
        } else {
            reminders
        }

        val finalFiltered = when (filterType) {
            MaintenanceFilterType.ALL -> queryFiltered
            MaintenanceFilterType.WARNINGS -> {
                queryFiltered.filter {
                    val warningStatusIds = setOf(2, 3) // Status IDs for Due Soon, Overdue
                    warningStatusIds.contains(it.statusId)
                   }
            }
            MaintenanceFilterType.TYPE -> {
                if (typeId != null) {
                    reminders.filter { it.typeId == typeId }
                } else {
                    queryFiltered
                }
            }
        }
        Log.d(logTag, "Filtering resulted in ${finalFiltered.size} reminders.")
        return finalFiltered
    }

    /** Refreshes the reminder list for the currently selected vehicle. */
    fun refreshReminders() {
        Log.d(logTag,"Refresh reminders requested.")
        _uiState.value.selectedVehicleId?.let { fetchReminders(it) }
            ?: Log.w(logTag, "Refresh requested but no vehicle is selected.")
    }
}