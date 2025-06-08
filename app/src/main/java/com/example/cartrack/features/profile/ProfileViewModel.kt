package com.example.cartrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.UserRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager,
    private val tokenManager: TokenManager, // Păstrăm pentru a obține token-ul
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Observă vehiculul activ în timp real pentru a afișa bifa corect
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
            val token = tokenManager.accessTokenFlow.firstOrNull()
            val clientId = jwtDecoder.getClientIdFromToken(token)

            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User session not found.") }
                return@launch
            }

            val userDeferred = async { userRepository.getUserInfo() }
            val vehiclesDeferred = async { vehicleRepository.getVehiclesByClientId(clientId) }

            val userResult = userDeferred.await()
            val vehiclesResult = vehiclesDeferred.await()

            var finalError: String? = null

            userResult.onFailure { finalError = it.message }
            vehiclesResult.onFailure { finalError = (finalError?.plus("\n") ?: "") + it.message }

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
}