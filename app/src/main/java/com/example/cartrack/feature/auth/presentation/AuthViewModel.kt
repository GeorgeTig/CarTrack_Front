package com.example.cartrack.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder
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
    private val authRepository: AuthRepository,
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val isLoggedIn = authRepository.isLoggedIn()
    val hasVehicles = authRepository.hasVehicles()

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
            _uiState.update { it.copy(isLoading = true, error = null, isLoginSuccess = false) }

            val request = UserLoginRequest(email.trim(), password)
            val result1 = authRepository.login(request)

            result1.onSuccess {
                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid client token") }
                    return@launch
                }

                val result2 = authRepository.hasVehicles(clientId)

                result2.onSuccess {
                    _uiState.update { it.copy(isLoading = false,hasVehicle = true, isLoginSuccess = true) }
                }.onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false,hasVehicle = false, isLoginSuccess = true) }
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "An unknown login error occurred."
                    )
                }
            }
        }

    }

    fun validateEmail(email: String) {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        if (!email.matches(emailRegex)) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address.") }
        } else {
            _uiState.update { it.copy(emailError = null) }
        }
    }

    fun validateUsername(username: String) {
        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username is required.") }
        } else {
            _uiState.update { it.copy(usernameError = null) }
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 8) {
            _uiState.update { it.copy(passwordError = "Password must be at least 8 characters long.") }
        } else {
            _uiState.update { it.copy(passwordError = null) }
        }
    }

    fun validatePhoneNumber(phoneNumber: String) {
        if (phoneNumber.isNotBlank() && !phoneNumber.matches("^\\+?[0-9]{10,15}$".toRegex()) && phoneNumber.length!= 10) {
            _uiState.update { it.copy(phoneNumberError = "Please enter a valid phone number.") }
        } else {
            _uiState.update { it.copy(phoneNumberError = null) }
        }
    }

    /**
     * Attempts to register a new user. Updates UI state based on the result.
     */
    fun register(username: String, email: String, password: String, phoneNumber: String) {

        validatePassword(password)
        validateEmail(email)
        validateUsername(username)
        validatePhoneNumber(phoneNumber)

        if (_uiState.value.phoneNumberError != null || _uiState.value.emailError != null || _uiState.value.usernameError != null || _uiState.value.passwordError != null) {
            return
        }

        viewModelScope.launch {
            // Set loading state and clear previous errors/success flags
            _uiState.update { it.copy(isLoading = true, error = null, isRegisterSuccess = false) }

            val request = UserRegisterRequest(
                username = username.trim(),
                email = email.trim(),
                password = password,
                phoneNumber = phoneNumber.trim(),
                roleId = 1 // roleId must be 1 (user). Only users can register!
            )
            val result = authRepository.register(request)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
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
            authRepository.logout()

            _uiState.update { it.copy(isLoading = false, isLoginSuccess = false, isRegisterSuccess = false, error = null) }
        }
    }
}