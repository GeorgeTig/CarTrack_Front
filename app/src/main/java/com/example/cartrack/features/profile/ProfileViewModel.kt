package com.example.cartrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.UserRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val userDeferred = async { userRepository.getUserInfo() }
            val vehiclesDeferred = async { vehicleRepository.getVehiclesByClientId() }

            val userResult = userDeferred.await()
            val vehiclesResult = vehiclesDeferred.await()

            var finalError: String? = null
            userResult.onFailure { finalError = it.message }
            vehiclesResult.onFailure {
                finalError = (finalError?.plus("\n") ?: "") + it.message
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userInfo = userResult.getOrNull(),
                    vehicles = vehiclesResult.getOrNull() ?: emptyList(),
                    error = finalError,
                    vehicleToDelete = null,
                    isDeleteLoading = false
                )
            }
        }
    }

    fun onShowDeleteDialog(vehicleId: Int) {
        _uiState.update { it.copy(vehicleToDelete = vehicleId) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(vehicleToDelete = null, isDeleteLoading = false) }
    }

    fun confirmDeleteVehicle() {
        val vehicleId = _uiState.value.vehicleToDelete ?: return

        _uiState.update { it.copy(isDeleteLoading = true) }

        viewModelScope.launch {
            vehicleRepository.deactivateVehicle(vehicleId).onSuccess {
                loadProfileData()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isDeleteLoading = false, vehicleToDelete = null) }
            }
        }
    }
}