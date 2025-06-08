package com.example.cartrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.UserRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        vehicleManager.lastVehicleIdFlow
            .onEach { activeId ->
                _uiState.update { it.copy(activeVehicleId = activeId) }
            }
            .launchIn(viewModelScope)

        loadProfileData()
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Rulăm ambele apeluri de rețea în paralel pentru eficiență
            val userDeferred = async { userRepository.getUserInfo() }
            val vehiclesDeferred = async { vehicleRepository.getVehiclesByClientId() }

            val userResult = userDeferred.await()
            val vehiclesResult = vehiclesDeferred.await()

            var finalError: String? = null

            userResult.onFailure {
                finalError = it.message
            }
            vehiclesResult.onFailure {
                // Adaugă eroarea la cea existentă, dacă există
                finalError = (finalError?.plus("\n") ?: "") + it.message
            }

            // Actualizăm starea o singură dată la final, cu toate rezultatele
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userInfo = userResult.getOrNull(),
                    vehicles = vehiclesResult.getOrNull() ?: emptyList(),
                    error = finalError
                )
            }
        }
    }

    fun setActiveVehicle(vehicleId: Int) {
        viewModelScope.launch {
            // Doar salvăm noul ID activ. Flow-ul va notifica UI-ul.
            vehicleManager.saveLastVehicleId(vehicleId)
        }
    }
}