package com.example.cartrack.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import com.example.cartrack.feature.profile.data.model.UserResponseDto
import com.example.cartrack.feature.profile.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val jwtDecoder: JwtDecoder,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Observă vehiculul activ în timp real din cache
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
            val clientId = jwtDecoder.getClientIdFromToken()

            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not identified.") }
                return@launch
            }

            var userInfoResult: UserResponseDto? = null
            var vehiclesResult: List<VehicleResponseDto> = emptyList()
            var currentError: String? = null

            // Fetch User Info
            userRepository.getUserInfo(clientId)
                .onSuccess { userInfo ->
                    userInfoResult = userInfo
                }
                .onFailure { e ->
                    currentError = "Failed to load user details: ${e.message}"
                }

            // Fetch Vehicles
            if (currentError == null) {
                vehicleRepository.getVehiclesByClientId(clientId)
                    .onSuccess { vehicles ->
                        vehiclesResult = vehicles
                    }
                    .onFailure { e ->
                        currentError = (currentError?.plus("\n") ?: "") + "Failed to load vehicles: ${e.message}"
                    }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userInfo = userInfoResult,
                    vehicles = vehiclesResult,
                    error = currentError
                )
            }
        }
    }

    fun setActiveVehicle(vehicleId: Int) {
        viewModelScope.launch {
            vehicleManager.saveLastVehicleId(vehicleId)
        }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(dialogType = ProfileConfirmationDialog.Logout) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogType = null) }
    }
}