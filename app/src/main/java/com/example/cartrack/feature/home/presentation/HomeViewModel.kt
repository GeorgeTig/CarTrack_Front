package com.example.cartrack.feature.home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository // Ensure this has the detail methods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder,
    private val vehicleCacheManager: VehicleManager // Inject the interface
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // No longer need to expose selectedVehicleIdFlow from here

    private val logTag = "HomeViewModel"

    init {
        loadInitialVehicleData()
    }

    fun loadInitialVehicleData() {
        // Avoid re-triggering full load if already done/in progress
        if (!_uiState.value.isLoadingVehicleList && _uiState.value.selectedVehicle != null) {
            Log.d(logTag, "Initial vehicle data already loaded, skipping full reload.")
            return
        }
        _uiState.update { it.copy(isLoadingVehicleList = true, vehicleListError = null) }
        Log.d(logTag, "Loading initial vehicle data...")

        viewModelScope.launch {
            val userId = jwtDecoder.getClientIdFromToken()
            if (userId == null) {
                Log.e(logTag, "Failed to get User ID.")
                _uiState.update { it.copy(isLoadingVehicleList = false, vehicleListError = "Could not verify user.") }
                return@launch
            }

            val vehiclesResult = vehicleRepository.getVehiclesByClientId(userId)
            vehiclesResult.onSuccess { fetchedVehicles ->
                Log.d(logTag, "Fetched ${fetchedVehicles.size} vehicles.")
                if (fetchedVehicles.isEmpty()) {
                    // Update state to reflect no vehicles. MainViewModel handles redirect.
                    _uiState.update {
                        it.copy(
                            isLoadingVehicleList = false,
                            vehicles = emptyList(),
                            selectedVehicle = null,
                            dropdownVehicles = emptyList(),
                            vehicleListError = "No vehicles found for this user."
                        )
                    }
                } else {
                    // Read initial value from cache manager's state flow
                    val lastUsedId = vehicleCacheManager.lastVehicleIdFlow.firstOrNull()
                    Log.d(logTag, "Initial Last used ID from cache state: $lastUsedId")

                    val currentSelected = lastUsedId?.let { cachedId ->
                        fetchedVehicles.find { it.id == cachedId }
                    } ?: fetchedVehicles.first()

                    // Save selected ID (which updates the StateFlow via DataStore)
                    // Do this *before* updating state if other VMs react immediately
                    vehicleCacheManager.saveLastVehicleId(currentSelected.id)

                    val others = fetchedVehicles.filter { it.id != currentSelected.id }

                    _uiState.update {
                        it.copy(
                            isLoadingVehicleList = false,
                            vehicles = fetchedVehicles,
                            selectedVehicle = currentSelected,
                            dropdownVehicles = others,
                            vehicleListError = null
                        )
                    }
                    Log.d(logTag, "Initial vehicle selected: ID ${currentSelected.id}")
                }
            }.onFailure { exception ->
                Log.e(logTag, "Error fetching vehicles: ${exception.message}", exception)
                _uiState.update {
                    it.copy(isLoadingVehicleList = false, vehicleListError = exception.message ?: "Failed to load vehicles.")
                }
            }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        val selected = _uiState.value.vehicles.find { it.id == vehicleId }
        if (selected != null && selected.id != _uiState.value.selectedVehicle?.id) {
            viewModelScope.launch {
                // Save selected ID (this will trigger update in observer VMs)
                vehicleCacheManager.saveLastVehicleId(selected.id)

                // Update this VM's state
                val others = _uiState.value.vehicles.filter { it.id != selected.id }
                _uiState.update {
                    it.copy(
                        selectedVehicle = selected,
                        dropdownVehicles = others,
                        isDropdownExpanded = false
                    )
                }
                Log.d(logTag, "User selected vehicle: ${selected.series} (ID: ${selected.id})")
            }
        } else {
            // Just close dropdown if same vehicle clicked
            _uiState.update { it.copy(isDropdownExpanded = false) }
        }
    }

    fun toggleDropdown(expanded: Boolean? = null) {
        _uiState.update { it.copy(isDropdownExpanded = expanded ?: !it.isDropdownExpanded) }
    }

    fun selectTab(tab: HomeTab) {
        if (_uiState.value.selectedTab != tab) {
            Log.d(logTag, "Switching tab to $tab")
            _uiState.update { it.copy(selectedTab = tab) }
        }
    }

}