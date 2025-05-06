package com.example.cartrack.feature.home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder,
    private val vehicleCacheManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val logTag = "HomeViewModel"

    init {
        loadVehiclesAndSetSelected()
    }

    fun loadVehiclesAndSetSelected() {
        if (_uiState.value.isLoading) { // Avoid multiple concurrent loads if already loading
            Log.d(logTag, "Already loading, refresh skipped initially.")
            // return // Optional: uncomment if you want to prevent re-triggering while already loading
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        Log.d(logTag, "Loading vehicles...")

        viewModelScope.launch {
            val userId = jwtDecoder.getClientIdFromToken()
            if (userId == null) {
                Log.e(logTag, "Failed to get User ID.")
                _uiState.update { it.copy(isLoading = false, error = "Could not verify user.") }
                return@launch
            }

            val vehiclesResult = vehicleRepository.getVehiclesByClientId(userId)
            vehiclesResult.onSuccess { fetchedVehicles ->
                Log.d(logTag, "Fetched ${fetchedVehicles.size} vehicles.")
                if (fetchedVehicles.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, dropdownVehicles = emptyList(), selectedVehicle = null, error = "No vehicles found for this user.") }
                } else {
                    val lastUsedId = vehicleCacheManager.lastVehicleIdFlow.firstOrNull()
                    Log.d(logTag, "Last used ID from cache: $lastUsedId")
                    val currentSelected = lastUsedId?.let { cachedId ->
                        fetchedVehicles.find { it.id == cachedId }
                    } ?: fetchedVehicles.first()

                    vehicleCacheManager.saveLastVehicleId(currentSelected.id)
                    Log.d(logTag, "Selected vehicle: ${currentSelected.series} (ID: ${currentSelected.id})")

                    // eliminete the vehicle from the list

                    val filteredVehicles = fetchedVehicles.filter { it.id != currentSelected.id }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dropdownVehicles = filteredVehicles,
                            vehicles = fetchedVehicles,
                            selectedVehicle = currentSelected,
                            error = null
                        )
                    }
                }
            }.onFailure { exception ->
                Log.e(logTag, "Error fetching vehicles: ${exception.message}", exception)
                _uiState.update {
                    it.copy(isLoading = false, error = exception.message ?: "Failed to load vehicles.")
                }
            }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        val selected = _uiState.value.dropdownVehicles.find { it.id == vehicleId }
        if (selected != null && selected.id != _uiState.value.selectedVehicle?.id) {
            viewModelScope.launch {
                vehicleCacheManager.saveLastVehicleId(selected.id)
                val filteredVehicles = _uiState.value.vehicles.filter { it.id != selected.id }
                _uiState.update { it.copy(selectedVehicle = selected, dropdownVehicles = filteredVehicles, isDropdownExpanded = false) }
                Log.d(logTag, "User selected vehicle: ${selected.series} (ID: ${selected.id})")
            }
        } else {
            // Close dropdown even if same vehicle selected or ID not found (should not happen)
            _uiState.update { it.copy(isDropdownExpanded = false) }
        }
    }

    fun toggleDropdown(expanded: Boolean? = null) {
        _uiState.update { it.copy(isDropdownExpanded = expanded ?: !it.isDropdownExpanded) }
    }

    fun refreshData() {
        Log.d(logTag, "Refresh data triggered.")
        loadVehiclesAndSetSelected() // Re-run the loading logic
    }
}