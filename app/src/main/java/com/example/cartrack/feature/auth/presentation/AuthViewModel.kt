package com.example.cartrack.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import com.example.cartrack.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository // Inject the repository interface
) : ViewModel() {

    // Private mutable state flow for internal updates
    private val _uiState = MutableStateFlow(AuthUiState())
    // Public immutable state flow for UI observation
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Expose login status Flow directly from the repository
    // UI can collect this to determine initial navigation or current state
    val isLoggedIn = authRepository.isLoggedIn()

    /**
     * Attempts to log in the user. Updates UI state based on the result.
     */
    fun login(email: String, password: String) {
        // Basic validation (optional, can also be done in UI)
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty.") }
            return
        }

        viewModelScope.launch {
            // Set loading state and clear previous errors/success flags
            _uiState.update { it.copy(isLoading = true, error = null, isLoginSuccess = false) }

            val request = UserLoginRequest(email = email.trim(), password = password) // Trim whitespace
            val result = authRepository.login(request)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // Use the user-friendly message from the repository's Result.failure
                        error = exception.message ?: "An unknown login error occurred."
                    )
                }
            }
        }
    }

    /**
     * Attempts to register a new user. Updates UI state based on the result.
     */
    fun register(username: String, email: String, password: String, phoneNumber: String) {
        // Basic validation (optional)
        if (username.isBlank() || email.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
            _uiState.update { it.copy(error = "All fields are required for registration.") }
            return
        }
        // Add more specific validation if needed (e.g., email format, password strength)

        viewModelScope.launch {
            // Set loading state and clear previous errors/success flags
            _uiState.update { it.copy(isLoading = true, error = null, isRegisterSuccess = false) }

            // Assuming roleId 1 for regular user - adjust if needed based on backend/logic
            val request = UserRegisterRequest(
                username = username.trim(),
                email = email.trim(),
                password = password, // Don't trim password
                phoneNumber = phoneNumber.trim(),
                roleId = 1
            )
            val result = authRepository.register(request)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // Use the user-friendly message from the repository's Result.failure
                        error = exception.message ?: "An unknown registration error occurred."
                    )
                }
            }
        }
    }

    /**
     * Call this function from the UI after navigation triggered by login success has been handled.
     */
    fun resetLoginSuccessHandled() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    /**
     * Call this function from the UI after navigation/feedback triggered by register success has been handled.
     */
    fun resetRegisterSuccessHandled() {
        _uiState.update { it.copy(isRegisterSuccess = false) }
    }

    /**
     * Call this function from the UI after an error message has been displayed to the user.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Logs the user out by deleting the local token.
     */
    fun logout() {
        viewModelScope.launch {
            // Optionally update UI state if needed (e.g., show loading spinner during logout)
            // _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            // No specific success state needed here, the isLoggedIn flow will automatically update
            // Reset any lingering flags just in case
            _uiState.update { it.copy(isLoading = false, isLoginSuccess = false, isRegisterSuccess = false, error = null) }
        }
    }
}