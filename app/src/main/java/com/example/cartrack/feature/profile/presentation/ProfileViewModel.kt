package com.example.cartrack.feature.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.UserManager // Pentru clientId (dacă nu e din JwtDecoder)
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository // Pentru lista de vehicule
import com.example.cartrack.feature.profile.data.model.UserResponseDto
import com.example.cartrack.feature.profile.domain.repository.UserRepository // Noul repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val jwtDecoder: JwtDecoder, // Sau injectează UserManager pentru a lua clientId
    // private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val logTag = "ProfileViewModel"

    init {
        Log.d(logTag, "ProfileViewModel initialized.")
        loadProfileData()
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // val clientId = userManager.clientIdFlow.firstOrNull() // Alternativă
            val clientId = jwtDecoder.getClientIdFromToken()

            if (clientId == null) {
                Log.e(logTag, "Client ID is null. Cannot load profile data.")
                _uiState.update { it.copy(isLoading = false, error = "User not identified.") }
                return@launch
            }
            Log.d(logTag, "Fetching data for client ID: $clientId")

            var userInfoResult: UserResponseDto? = null
            var vehiclesResult: List<VehicleResponseDto> = emptyList()
            var currentError: String? = null

            // Fetch User Info
            userRepository.getUserInfo(clientId)
                .onSuccess { userInfo ->
                    Log.d(logTag, "User info fetched: ${userInfo.username}")
                    userInfoResult = userInfo
                }
                .onFailure { e ->
                    Log.e(logTag, "Failed to fetch user info: ${e.message}")
                    currentError = "Failed to load user details: ${e.message}"
                }

            // Fetch Vehicles (doar dacă user info a fost ok sau vrei să încerci oricum)
            if (currentError == null) { // Sau poți face fetch-ul în paralel
                vehicleRepository.getVehiclesByClientId(clientId)
                    .onSuccess { vehicles ->
                        Log.d(logTag, "Vehicles fetched: ${vehicles.size} vehicles.")
                        vehiclesResult = vehicles
                    }
                    .onFailure { e ->
                        Log.e(logTag, "Failed to fetch vehicles: ${e.message}")
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
}