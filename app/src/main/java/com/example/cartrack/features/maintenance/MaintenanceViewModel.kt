package com.example.cartrack.features.maintenance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.getIconForMaintenanceType
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.components.FilterChipData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
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
            vehicleManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to: $vehicleId")
                    _uiState.update { it.copy(selectedVehicleId = vehicleId) }
                    if (vehicleId != null) {
                        fetchRemindersForVehicle(vehicleId)
                    } else {
                        // Clear data if no vehicle is selected
                        _uiState.update { it.copy(reminders = emptyList(), filteredReminders = emptyList(), availableTypes = emptyList()) }
                    }
                }
        }
    }

    fun fetchRemindersForVehicle(vehicleId: Int, isRetry: Boolean = false) {
        if (!isRetry) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(error = null, isLoading = true) }
        }

        viewModelScope.launch {
            vehicleRepository.getRemindersByVehicleId(vehicleId).onSuccess { data ->
                val types = data.filter { it.typeName.isNotBlank() }
                    .distinctBy { it.typeId }
                    .map { FilterChipData(it.typeId, it.typeName, getIconForMaintenanceType(it.typeId)) }
                    .sortedBy { it.name }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        reminders = data,
                        availableTypes = types,
                        filteredReminders = applyAllFilters(data, currentState.searchQuery, currentState.selectedMainTab, currentState.selectedTypeId)
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredReminders = applyAllFilters(currentState.reminders, query, currentState.selectedMainTab, currentState.selectedTypeId)
            )
        }
    }

    fun selectMainTab(tab: MaintenanceMainTab) {
        if (_uiState.value.selectedMainTab == tab) return
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                selectedMainTab = tab,
                filteredReminders = applyAllFilters(currentState.reminders, currentState.searchQuery, tab, currentState.selectedTypeId)
            )
        }
    }

    fun selectTypeFilter(typeId: Int?) {
        val currentState = _uiState.value
        _uiState.update {
            it.copy(
                selectedTypeId = typeId,
                filteredReminders = applyAllFilters(currentState.reminders, currentState.searchQuery, currentState.selectedMainTab, typeId)
            )
        }
    }

    private fun applyAllFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        mainTab: MaintenanceMainTab,
        typeId: Int?
    ): List<ReminderResponseDto> {
        val tabFiltered = when (mainTab) {
            MaintenanceMainTab.ACTIVE -> reminders.filter { it.isActive }
            MaintenanceMainTab.INACTIVE -> reminders.filter { !it.isActive }
            MaintenanceMainTab.WARNINGS -> reminders.filter { it.isActive && it.statusId in 2..3 }
        }

        val typeFiltered = if (typeId != null) {
            tabFiltered.filter { it.typeId == typeId }
        } else {
            tabFiltered
        }

        return if (query.isNotBlank()) {
            typeFiltered.filter {
                it.name.contains(query, ignoreCase = true) || it.typeName.contains(query, ignoreCase = true)
            }
        } else {
            typeFiltered
        }
    }
}