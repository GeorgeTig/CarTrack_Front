package com.example.cartrack.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.services.signalr.SignalRService
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Definim un eveniment sigilat pentru comunicarea cu UI-ul
sealed class AuthEvent {
    object RequestAppReset : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signalRService: SignalRService,
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasNewNotifications: StateFlow<Boolean> = userManager.hasNewNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isSessionCheckComplete = MutableStateFlow(false)
    val isSessionCheckComplete: StateFlow<Boolean> = _isSessionCheckComplete.asStateFlow()

    init {
        performInitialSessionCheck()
        observeLoginStatusForSignalR()
    }

    private fun performInitialSessionCheck() {
        viewModelScope.launch {
            val hasToken = tokenManager.accessTokenFlow.firstOrNull()?.isNotBlank() ?: false
            if (hasToken) {
                authRepository.attemptSilentRefresh()
            }
            _isSessionCheckComplete.value = true
            Log.d("AuthViewModel", "Initial session check complete.")
        }
    }

    private fun observeLoginStatusForSignalR() {
        viewModelScope.launch {
            isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    Log.d("AuthViewModel", "User is logged in. Starting SignalR.")
                    signalRService.startConnection()
                } else {
                    Log.d("AuthViewModel", "User is not logged in. Stopping SignalR.")
                    signalRService.stopConnection()
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            signalRService.stopConnection()
            authRepository.logout()
            _events.emit(AuthEvent.RequestAppReset) // Emitem evenimentul de reset
        }
    }

    // --- Logic for Login (rămâne neschimbată) ---
    fun onLoginEmailChanged(email: String) { _uiState.update { it.copy(emailLogin = email, emailErrorLogin = null, generalError = null) } }
    fun onLoginPasswordChanged(password: String) { _uiState.update { it.copy(passwordLogin = password, passwordErrorLogin = null, generalError = null) } }

    private fun validateLoginFields(): Boolean {
        val state = _uiState.value
        var isValid = true
        _uiState.update { it.copy(emailErrorLogin = null, passwordErrorLogin = null) }
        if (state.emailLogin.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.emailLogin).matches()) {
            _uiState.update { it.copy(emailErrorLogin = "Please enter a valid email.") }
            isValid = false
        }
        if (state.passwordLogin.isBlank()) {
            _uiState.update { it.copy(passwordErrorLogin = "Password cannot be empty.") }
            isValid = false
        }
        return isValid
    }

    fun login() {
        if (!validateLoginFields()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val request = UserLoginRequestDto(_uiState.value.emailLogin.trim(), _uiState.value.passwordLogin)
            authRepository.login(request).onSuccess {
                authRepository.hasVehicles().onSuccess {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true, requiresVehicleAddition = false) }
                }.onFailure {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true, requiresVehicleAddition = true) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, generalError = e.message) }
            }
        }
    }

    // --- Logic for Registration (rămâne neschimbată) ---
    fun onRegisterUsernameChanged(name: String) { _uiState.update { it.copy(usernameRegister = name, usernameErrorRegister = null, generalError = null) } }
    fun onRegisterEmailChanged(email: String) { _uiState.update { it.copy(emailRegister = email, emailErrorRegister = null, generalError = null) } }
    fun onRegisterPhoneNumberChanged(phone: String) { _uiState.update { it.copy(phoneNumberRegister = phone.filter { it.isDigit() }, phoneNumberErrorRegister = null, generalError = null) } }
    fun onRegisterTermsAcceptedChanged(accepted: Boolean) { _uiState.update { it.copy(termsAcceptedRegister = accepted, termsErrorRegister = null, generalError = null) } }

    fun onRegisterPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordRegister = password, passwordErrorRegister = null, generalError = null) }
        updatePasswordFeedback(password)
        if (_uiState.value.confirmPasswordRegister.isNotBlank()) {
            validateConfirmPassword(password, _uiState.value.confirmPasswordRegister, forSubmit = false)
        }
    }

    fun onRegisterConfirmPasswordChanged(confirm: String) {
        _uiState.update { it.copy(confirmPasswordRegister = confirm, confirmPasswordErrorRegister = null) }
        validateConfirmPassword(_uiState.value.passwordRegister, confirm, forSubmit = false)
    }

    private fun updatePasswordFeedback(password: String) {
        val requirements = listOf(
            "Minimum 8 characters" to (password.length >= 8),
            "Contains an uppercase letter" to password.any { it.isUpperCase() },
            "Contains a lowercase letter" to password.any { it.isLowerCase() },
            "Contains a digit" to password.any { it.isDigit() }
        )
        val strengthScore = requirements.count { it.second }
        val strength = when {
            password.isEmpty() -> PasswordStrength.NONE
            strengthScore <= 2 -> PasswordStrength.WEAK
            strengthScore == 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
        _uiState.update { it.copy(passwordStrengthRegister = strength, passwordRequirementsMet = requirements) }
    }

    private fun validateConfirmPassword(password: String, confirm: String, forSubmit: Boolean) {
        if (forSubmit && confirm.isBlank()) {
            _uiState.update { it.copy(confirmPasswordErrorRegister = "Please confirm your password.") }
        } else if (password != confirm && (forSubmit || confirm.isNotBlank())) {
            _uiState.update { it.copy(confirmPasswordErrorRegister = "Passwords do not match.") }
        } else {
            _uiState.update { it.copy(confirmPasswordErrorRegister = null) }
        }
    }

    private fun validateRegistration(): Boolean {
        val state = _uiState.value
        _uiState.update { it.copy(usernameErrorRegister = null, emailErrorRegister = null, phoneNumberErrorRegister = null, passwordErrorRegister = null, confirmPasswordErrorRegister = null, termsErrorRegister = null) }
        var isValid = true
        if (state.usernameRegister.isBlank()) { _uiState.update { it.copy(usernameErrorRegister = "Username is required.") }; isValid = false }
        if (state.emailRegister.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(state.emailRegister).matches()) { _uiState.update { it.copy(emailErrorRegister = "A valid email is required.") }; isValid = false }
        if (state.phoneNumberRegister.isNotEmpty() && state.phoneNumberRegister.length < 10) { _uiState.update { it.copy(phoneNumberErrorRegister = "Phone must be at least 10 digits.") }; isValid = false }
        if (!state.passwordRequirementsMet.all { it.second }) { _uiState.update { it.copy(passwordErrorRegister = "Password does not meet all requirements.") }; isValid = false }
        validateConfirmPassword(state.passwordRegister, state.confirmPasswordRegister, forSubmit = true)
        if (_uiState.value.confirmPasswordErrorRegister != null) isValid = false
        if (!state.termsAcceptedRegister) { _uiState.update { it.copy(termsErrorRegister = "You must accept the terms and conditions.") }; isValid = false }
        return isValid
    }

    fun register() {
        if (!validateRegistration()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val request = UserRegisterRequestDto(
                username = _uiState.value.usernameRegister.trim(),
                email = _uiState.value.emailRegister.trim(),
                password = _uiState.value.passwordRegister,
                phoneNumber = _uiState.value.phoneNumberRegister.ifBlank { "0" },
                roleId = 1
            )
            authRepository.register(request).onSuccess {
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true, successMessage = "Registration successful! Please login.") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, generalError = e.message) }
            }
        }
    }

    // --- State Reset Functions ---
    fun clearGeneralError() { _uiState.update { it.copy(generalError = null) } }
    fun resetLoginSuccess() { _uiState.update { it.copy(isLoginSuccess = false, requiresVehicleAddition = false) } }
    fun resetRegisterSuccess() { _uiState.update { it.copy(isRegisterSuccess = false, successMessage = null) } }
}