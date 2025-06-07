package com.example.cartrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.UserRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
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
        observeActiveVehicle()
        loadProfileData()
    }

    private fun observeActiveVehicle() {
        vehicleManager.lastVehicleIdFlow
            .onEach { activeId -> _uiState.update { it.copy(activeVehicleId = activeId) } }
            .launchIn(viewModelScope)
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val token = (vehicleManager as com.example.cartrack.core.storage.TokenManager).accessTokenFlow.firstOrNull() // Temporar
            val clientId = jwtDecoder.getClientIdFromToken(token)

            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not identified.") }
                return@launch
            }

            try {
                // Rulează ambele apeluri în paralel pentru eficiență
                coroutineScope {
                    val userDeferred = async { userRepository.getUserInfo() }
                    val vehiclesDeferred = async { vehicleRepository.getVehiclesByClientId(clientId) }

                    val userResult = userDeferred.await()
                    val vehiclesResult = vehiclesDeferred.await()

                    val finalError = listOf(userResult, vehiclesResult).mapNotNull { it.exceptionOrNull()?.message }.joinToString("\n")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userInfo = userResult.getOrNull(),
                            vehicles = vehiclesResult.getOrNull() ?: emptyList(),
                            error = finalError.ifBlank { null }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "An unexpected error occurred.") }
            }
        }
    }

    fun setActiveVehicle(vehicleId: Int) {
        viewModelScope.launch {
            vehicleManager.saveLastVehicleId(vehicleId)
        }
    }
}