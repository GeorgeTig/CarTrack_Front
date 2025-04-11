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
    private val vinDecoderRepository: VinDecoderRepository, // Inject repo from this feature
    private val jwtDecoder: JwtDecoder // Inject core utility
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddVehicleUiState())
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    // Called when the VIN TextField value changes in the UI
    fun onVinInputChange(newVin: String) {
        // Basic input processing: filter non-alphanumeric, limit length, uppercase
        val processedVin = newVin.filter { it.isLetterOrDigit() }.uppercase()
        if (processedVin.length <= 17) { // Enforce max length
            _uiState.update {
                it.copy(
                    vinInput = processedVin,
                    vinValidationError = null, // Clear validation error when user types
                    error = null, // Clear general error
                    decodeResult = null // Clear previous results when VIN changes
                )
            }
        }
    }

    // Called when the "Decode VIN" button is clicked
    fun decodeVin() {
        val vin = _uiState.value.vinInput

        // Validate VIN length before making API call
        if (vin.length != 17) {
            _uiState.update { it.copy(vinValidationError = "VIN must be exactly 17 characters.") }
            return // Stop if VIN length is wrong
        }
        _uiState.update { it.copy(vinValidationError = null) } // Clear validation error if length is ok

        // Launch network request in ViewModelScope
        viewModelScope.launch {
            // Set loading state, clear errors and previous results
            _uiState.update { it.copy(isLoading = true, error = null, decodeResult = null) }

            // Get Client ID from JWT (necessary for API call)
            val clientId = jwtDecoder.getClientIdFromToken()
            if (clientId == null) {
                // If clientId can't be found, update state with error and stop
                _uiState.update { it.copy(isLoading = false, error = "Cannot identify user. Please login again.") }
                return@launch
            }

            // Call the repository to decode the VIN
            val result = vinDecoderRepository.decodeVin(vin, clientId)

            // Handle the Result from the repository
            result.onSuccess { decodedInfo ->
                // Update state on success
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        decodeResult = decodedInfo, // Store the list of results
                        // Set error message if backend returned empty list, otherwise clear error
                        error = if (decodedInfo.isEmpty()) "No vehicle information found for this VIN." else null
                    )
                }
            }.onFailure { exception ->
                // Update state on failure
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // Use the user-friendly error message from the Result's exception
                        error = exception.message ?: "Failed to decode VIN due to an unknown error."
                    )
                }
            }
        }
    }

    // Called by the UI after an error message has been displayed (e.g., in a Snackbar)
    fun errorShown() {
        _uiState.update { it.copy(error = null) } // Clear the error message
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