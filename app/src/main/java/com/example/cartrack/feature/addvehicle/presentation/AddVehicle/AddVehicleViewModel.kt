package com.example.cartrack.feature.addvehicle.presentation.AddVehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vinDecoderRepository: VinDecoderRepository,
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    // Called when the VIN TextField value changes in the UI
    fun onVinInputChange(newVin: String) {
        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase()
        if (processedVin.length <= 17) {
            _uiState.update {
                it.copy(
                    vinInput = processedVin,
                    vinValidationError = null,
                    error = null,
                    decodeResult = null
                )
            }
        }
    }

    // Called when the "Decode VIN" button is clicked
    fun decodeVin() {
        val vin = _uiState.value.vinInput

        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be exactly 17 characters.") }
            return
        }
        _uiState.update { it.copy(vinValidationError = null) }

        // Launch network request in ViewModelScope
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, decodeResult = null) }

            // Get Client ID from JWT
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Cannot identify user. Please login again.") }
                return@launch
            }

            // Call the repository to decode the VIN
            val result = vinDecoderRepository.decodeVin(vin, clientId)

            // Handle the Result from the repository
            result.onSuccess { decodedInfo ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        decodeResult = decodedInfo,
                        error =
                            if(decodedInfo.isEmpty())
                            "No vehicle information found for this VIN."
                            else null
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to decode VIN due to an unknown error."
                    )
                }
            }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clears the decodeResult from the state, typically called after
     * navigation has been triggered based on the result. Prevents re-navigation
     * on recomposition.
     */
    fun clearDecodeResult() {
        _uiState.update { it.copy(decodeResult = null) }
    }
}