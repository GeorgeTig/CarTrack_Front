package com.example.cartrack.feature.home.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto // Import
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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


    private var fetchVehicleInfoJob: Job? = null
    private val logTag = "HomeViewModel"

    init {
        Log.d(logTag, "HomeViewModel instance created/init called.")
        loadVehicles()

        // Observă schimbările la selectedVehicle pentru a încărca informațiile suplimentare (kilometraj)
        viewModelScope.launch {
            _uiState.map { it.selectedVehicle }
                .distinctUntilChanged()
                .collectLatest { vehicle -> // collectLatest anulează încărcarea anterioară dacă vehiculul se schimbă
                    fetchVehicleInfoJob?.cancel() // Anulează job-ul anterior
                    if (vehicle != null) {
                        Log.d(logTag, "Selected vehicle changed to ID ${vehicle.id}. Fetching info...")
                        fetchSelectedVehicleInfo(vehicle.id)
                    } else {
                        _uiState.update { it.copy(selectedVehicleInfo = null) } // Curăță info dacă nu e niciun vehicul selectat
                    }
                }
        }
    }

    fun loadVehicles() {
        if (!_uiState.value.isLoadingVehicleList && _uiState.value.selectedVehicle != null && _uiState.value.vehicles.isNotEmpty()) {
            Log.d(logTag, "Initial vehicle data likely loaded, skipping full reload to preserve selection.")
            // Poate totuși vrem să reîmprospătăm lista de vehicule în caz că s-a adăugat unul nou
            // Dar pentru UI-ul actual, ne concentrăm pe menținerea selecției.
            // Pentru a forța un refresh, elimină această condiție sau adaugă un parametru `forceRefresh`.
            return
        }
        _uiState.update { it.copy(isLoadingVehicleList = true, vehicleListError = null) }
        Log.d(logTag, "Loading initial vehicle data...")

        viewModelScope.launch {
            val userId = jwtDecoder.getClientIdFromToken()
            if (userId == null) {
                Log.e(logTag, "Failed to get User ID for loading vehicles.")
                _uiState.update { it.copy(isLoadingVehicleList = false, vehicleListError = "Could not verify user.") }
                return@launch
            }

            val vehiclesResult = vehicleRepository.getVehiclesByClientId(userId)
            vehiclesResult.onSuccess { fetchedVehicles ->
                Log.d(logTag, "Fetched ${fetchedVehicles.size} vehicles.")
                if (fetchedVehicles.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoadingVehicleList = false,
                            vehicles = emptyList(),
                            selectedVehicle = null,
                            selectedVehicleInfo = null, // Curăță și info
                            vehicleListError = "No vehicles found for this user."
                        )
                    }
                } else {
                    val lastUsedId = vehicleCacheManager.lastVehicleIdFlow.firstOrNull()
                    val currentSelected = lastUsedId?.let { cachedId ->
                        fetchedVehicles.find { it.id == cachedId }
                    } ?: fetchedVehicles.first()

                    _uiState.update {
                        it.copy(
                            isLoadingVehicleList = false,
                            vehicles = fetchedVehicles,
                            selectedVehicle = currentSelected,
                            // selectedVehicleInfo va fi încărcat de colectorul de mai sus
                            vehicleListError = null
                        )
                    }
                    Log.d(logTag, "Initial vehicle set: ID ${currentSelected.id}. Info will be fetched.")
                }
            }.onFailure { exception ->
                Log.e(logTag, "Error fetching vehicles: ${exception.message}", exception)
                _uiState.update {
                    it.copy(isLoadingVehicleList = false, vehicleListError = exception.message ?: "Failed to load vehicles.")
                }
            }
        }
    }

    private fun fetchSelectedVehicleInfo(vehicleId: Int) {
        fetchVehicleInfoJob = viewModelScope.launch {
            _uiState.update { it.copy(selectedVehicleInfo = null) } // Arată un placeholder/loading pentru info
            Log.d(logTag, "Fetching vehicle info for ID $vehicleId")
            val result = vehicleRepository.getVehicleInfo(vehicleId) // Presupunând că ai această metodă
            result.onSuccess { info ->
                Log.d(logTag, "Successfully fetched vehicle info: Mileage ${info.mileage}")
                _uiState.update { it.copy(selectedVehicleInfo = info) }
            }.onFailure { e ->
                Log.e(logTag, "Failed to fetch vehicle info for ID $vehicleId: ${e.message}", e)
                // Poți seta un mesaj de eroare specific pentru info dacă dorești
                _uiState.update { it.copy(selectedVehicleInfo = null) } // Lasă null sau un obiect de eroare
            }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        val newlySelectedVehicle = _uiState.value.vehicles.find { it.id == vehicleId }
        if (newlySelectedVehicle != null && newlySelectedVehicle.id != _uiState.value.selectedVehicle?.id) {
            Log.d(logTag, "User selected vehicle via UI: ID ${newlySelectedVehicle.id}")
            viewModelScope.launch {
                vehicleCacheManager.saveLastVehicleId(newlySelectedVehicle.id) // Salvează în cache selecția
            }
            // Actualizează selectedVehicle în UI state.
            // Colectorul din init se va ocupa de fetchSelectedVehicleInfo.
            _uiState.update {
                it.copy(
                    selectedVehicle = newlySelectedVehicle
                    // selectedVehicleInfo va fi setat la null temporar de colector, apoi reîncărcat
                )
            }
        }
    }
    // Nu mai avem nevoie de toggleDropdown și selectTab
}