package com.example.cartrack.features.maintenance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.cards.MaintenanceTypeIcon
import com.example.cartrack.core.ui.components.FilterChipData
import com.example.cartrack.navigation.Routes
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
        viewModelScope.launch {
            vehicleManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to: $vehicleId")
                    val currentTab = _uiState.value.selectedMainTab
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId, selectedMainTab = currentTab)
                    if (vehicleId != null) {
                        fetchReminders(vehicleId)
                    }
                }
        }
    }

    fun forceRefresh() {
        uiState.value.selectedVehicleId?.let {
            fetchReminders(it, isRetry = true)
        }
    }

    private fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        _uiState.update { it.copy(isLoading = !isRetry, error = null) }

        viewModelScope.launch {
            vehicleRepository.getRemindersByVehicleId(vehicleId).onSuccess { data ->
                val types = data.filter { it.typeName.isNotBlank() }
                    .distinctBy { it.typeId }
                    .map {
                        val typeIcon = MaintenanceTypeIcon.fromTypeId(it.typeId)
                        FilterChipData(it.typeId, it.typeName, typeIcon.icon)
                    }
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
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredReminders = applyAllFilters(it.reminders, query, it.selectedMainTab, it.selectedTypeId)
            )
        }
    }

    fun selectMainTab(tab: MaintenanceMainTab) {
        _uiState.update {
            it.copy(
                selectedMainTab = tab,
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, tab, it.selectedTypeId)
            )
        }
    }

    fun selectTypeFilter(typeId: Int?) {
        _uiState.update {
            it.copy(
                selectedTypeId = typeId,
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, it.selectedMainTab, typeId)
            )
        }
    }

    // --- MODIFICARE AICI: Funcția nu mai returnează un String ---
    fun onReminderClicked(reminderId: Int) {
        // În loc să construim ruta, emitem un eveniment cu ID-ul.
        viewModelScope.launch {
            _eventFlow.emit(MaintenanceEvent.NavigateToReminderDetail(reminderId))
        }
    }
    // --- SFÂRȘIT MODIFICARE ---

    private fun applyAllFilters(
        reminders: List<ReminderResponseDto>,
        query: String,
        mainTab: MaintenanceMainTab,
        typeId: Int?
    ): List<ReminderResponseDto> {
        val tabFiltered = when (mainTab) {
            MaintenanceMainTab.ACTIVE -> reminders.filter { it.isActive }
            MaintenanceMainTab.INACTIVE -> reminders.filter { !it.isActive }
            MaintenanceMainTab.WARNINGS -> reminders.filter { it.isActive && it.statusId in setOf(2, 3) }
        }

        val typeFiltered = if (typeId != null) tabFiltered.filter { it.typeId == typeId } else tabFiltered

        return if (query.isNotBlank()) {
            typeFiltered.filter { it.name.contains(query, ignoreCase = true) || it.typeName.contains(query, ignoreCase = true) }
        } else {
            typeFiltered
        }
    }
}