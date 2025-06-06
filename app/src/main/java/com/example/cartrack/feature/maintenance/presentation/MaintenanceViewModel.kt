package com.example.cartrack.feature.maintenance.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.ui.cards.ReminderCard.MaintenanceTypeIcon
import com.example.cartrack.core.vehicle.data.model.ReminderRequestDto
import com.example.cartrack.core.vehicle.data.model.ReminderResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import com.example.cartrack.feature.navigation.Routes
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

    private val _eventFlow = MutableSharedFlow<MaintenanceEvent>()
    val eventFlow: SharedFlow<MaintenanceEvent> = _eventFlow.asSharedFlow()

    private val logTag = "MaintenanceVM"

    init {
        startObservingSelectedVehicle()
    }

    private fun startObservingSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Selected vehicle changed to $vehicleId.")
                    val currentMainTab = _uiState.value.selectedMainTab
                    // Resetează starea pentru noul vehicul, păstrând tab-ul selectat
                    _uiState.value = MaintenanceUiState(selectedVehicleId = vehicleId, selectedMainTab = currentMainTab)
                    if (vehicleId != null) {
                        fetchReminders(vehicleId)
                    }
                }
        }
    }

    fun fetchReminders(vehicleId: Int, isRetry: Boolean = false) {
        if (!isRetry) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(error = null) } // Doar șterge eroarea la reîncercare
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
            }.onFailure { e ->
                Log.e(logTag, "Failed to fetch reminders: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load reminders.") }
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
        if (_uiState.value.selectedMainTab == tab) return
        Log.d(logTag, "Main tab selected: $tab")
        _uiState.update {
            it.copy(
                selectedMainTab = tab,
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, tab, it.selectedTypeId)
            )
        }
    }

    fun selectTypeFilter(typeId: Int?) {
        Log.d(logTag, "Type filter selected: ID ${typeId ?: "All"}")
        _uiState.update {
            it.copy(
                selectedTypeId = typeId,
                filteredReminders = applyAllFilters(it.reminders, it.searchQuery, it.selectedMainTab, typeId)
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
            MaintenanceMainTab.WARNINGS -> reminders.filter {
                it.isActive && setOf(2, 3).contains(it.statusId)
            }
        }

        val typeFiltered = if (typeId != null) {
            tabFiltered.filter { it.typeId == typeId }
        } else {
            tabFiltered
        }

        return if (query.isNotBlank()) {
            typeFiltered.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.typeName.contains(query, ignoreCase = true)
            }
        } else {
            typeFiltered
        }
    }

    /**
     * Called when a reminder item is clicked in the list.
     * It constructs the navigation route and passes it to the UI to execute.
     */
    fun onReminderItemClicked(reminder: ReminderResponseDto, onNavigate: (String) -> Unit) {
        // Indiferent de status, navighează la pagina de detalii
        onNavigate(Routes.reminderDetailRoute(reminder.configId))
    }

    // Funcțiile care gestionau dialogurile au fost eliminate:
    // - dismissReminderDetails()
    // - toggleReminderActiveStatus()
    // - showEditReminderDialog()
    // - dismissEditReminderDialog()
    // - onEdit...Changed()
    // - saveReminderEdits()
    // - restoreReminderToDefaults()
    // Toată această logică a fost mutată în ViewModel-urile corespunzătoare noilor ecrane.
}