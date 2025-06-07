package com.example.cartrack.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var fetchVehicleInfoJob: Job? = null
    private val logTag = "HomeViewModel"

    init {
        // Observă schimbările la vehiculul selectat pentru a-i încărca detaliile
        viewModelScope.launch {
            _uiState.map { it.selectedVehicle?.id }
                .distinctUntilChanged()
                .collectLatest { vehicleId ->
                    fetchVehicleInfoJob?.cancel()
                    if (vehicleId != null) {
                        fetchSelectedVehicleInfo(vehicleId)
                    }
                }
        }
    }

    fun loadVehicles(forceRefresh: Boolean = false) {
        if (_uiState.value.vehicles.isNotEmpty() && !forceRefresh) {
            Log.d(logTag, "Skipping vehicle load, data already present.")
            // Setează isLoading la false în caz că era true de la un refresh anterior
            if (_uiState.value.isLoading) _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Obține ID-ul clientului din token
            val token = (vehicleManager as com.example.cartrack.core.storage.TokenManager).accessTokenFlow.firstOrNull() // Temporar, trebuie refactorizat cu JwtDecoder în repo
            val clientId = jwtDecoder.getClientIdFromToken(token)

            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User session is invalid.") }
                return@launch
            }

            vehicleRepository.getVehiclesByClientId(clientId).onSuccess { fetchedVehicles ->
                if (fetchedVehicles.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, vehicles = emptyList(), selectedVehicle = null) }
                } else {
                    val lastUsedId = vehicleManager.lastVehicleIdFlow.firstOrNull()
                    val vehicleToSelect = fetchedVehicles.find { it.id == lastUsedId } ?: fetchedVehicles.first()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vehicles = fetchedVehicles,
                            selectedVehicle = vehicleToSelect
                        )
                    }
                    // Salvează vehiculul selectat pentru viitoarele deschideri ale aplicației
                    vehicleManager.saveLastVehicleId(vehicleToSelect.id)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun fetchSelectedVehicleInfo(vehicleId: Int) {
        // Arată un placeholder/loading pentru info
        _uiState.update { it.copy(selectedVehicleInfo = null) }
        fetchVehicleInfoJob = viewModelScope.launch {
            vehicleRepository.getVehicleInfo(vehicleId).onSuccess { info ->
                _uiState.update { it.copy(selectedVehicleInfo = info) }
            }.onFailure { e ->
                Log.e(logTag, "Failed to fetch info for vehicle $vehicleId: ${e.message}")
                // Poți seta o eroare specifică aici dacă dorești
            }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        val currentSelectedId = _uiState.value.selectedVehicle?.id
        if (vehicleId != currentSelectedId) {
            val newSelectedVehicle = _uiState.value.vehicles.find { it.id == vehicleId }
            if (newSelectedVehicle != null) {
                viewModelScope.launch {
                    vehicleManager.saveLastVehicleId(vehicleId)
                }
                _uiState.update { it.copy(selectedVehicle = newSelectedVehicle) }
            }
        }
    }
}