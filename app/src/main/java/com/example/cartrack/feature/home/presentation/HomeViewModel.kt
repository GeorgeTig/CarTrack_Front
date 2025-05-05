package com.example.cartrack.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.auth.domain.repository.AuthRepository
import com.example.cartrack.feature.auth.presentation.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
       setSelectedVehicleId()
    }

    fun setSelectedVehicleId() {

        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            _uiState.value = uiState.value.copy(selectedVehicleId = vehicleId?: -1)
        }
    }

}