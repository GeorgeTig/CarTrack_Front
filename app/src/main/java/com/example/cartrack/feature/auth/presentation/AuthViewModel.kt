package com.example.cartrack.feature.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.signalr.SignalRService
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import com.example.cartrack.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val authRepository: AuthRepository,
    val jwtDecoder: JwtDecoder,
    private val signalRService: SignalRService,
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L, 0L),
            initialValue = runBlocking { tokenManager.accessTokenFlow.firstOrNull()?.isNotBlank() ?: false }
        )

    val hasVehicles: StateFlow<Boolean> = authRepository.hasVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    private val _hasNewNotifications = MutableStateFlow(false)
    val hasNewNotifications: StateFlow<Boolean> = _hasNewNotifications.asStateFlow()

    private val _isSessionCheckComplete = MutableStateFlow(false)
    val isSessionCheckComplete: StateFlow<Boolean> = _isSessionCheckComplete.asStateFlow()

    companion object {
        @Volatile
        private var initialSessionCheckLogicHasRun = false
    }

    init {
        Log.d("AuthViewModel", "Instance created/init. initialSessionCheckLogicHasRun: $initialSessionCheckLogicHasRun")
        if (!initialSessionCheckLogicHasRun) {
            initialSessionCheckLogicHasRun = true
            Log.d("AuthViewModel", "INIT - Performing initial session check.")
            viewModelScope.launch {
                val initialToken = tokenManager.accessTokenFlow.firstOrNull()
                if (!initialToken.isNullOrBlank()) {
                    Log.d("AuthViewModel", "INIT - Initial token found. Attempting silent refresh.")
                    val refreshResult = authRepository.attemptSilentRefresh()
                    Log.i("AuthViewModel", "INIT - Silent refresh finished. Success: ${refreshResult.isSuccess}, Error: ${refreshResult.exceptionOrNull()?.message}")
                } else {
                    Log.d("AuthViewModel", "INIT - No initial token. Skipping silent refresh.")
                }
                _isSessionCheckComplete.value = true
                Log.d("AuthViewModel", "INIT - Session check complete flag set to true.")
            }
        } else {
            if (!_isSessionCheckComplete.value) _isSessionCheckComplete.value = true
            Log.d("AuthViewModel", "INIT - Initial session check logic already run for this app lifecycle.")
        }

        viewModelScope.launch {
            _isSessionCheckComplete.collectLatest { sessionCheckDone ->
                if (sessionCheckDone) {
                    Log.d("AuthViewModel", "Session check is complete. Starting to collect isLoggedIn for SignalR.")
                    isLoggedIn.collectLatest { loggedInStatus ->
                        Log.d("AuthViewModel", "isLoggedIn state for SignalR: $loggedInStatus")
                        if (loggedInStatus) {
                            val currentTokenForSignalR = tokenManager.accessTokenFlow.firstOrNull()
                            if (!currentTokenForSignalR.isNullOrBlank()) {
                                val clientId = jwtDecoder.getClientIdFromToken()
                                if (clientId != null) {
                                    Log.d("AuthViewModel", "User logged in (clientId: $clientId). Starting SignalR.")
                                    signalRService.startConnection()
                                } else {
                                    Log.w("AuthViewModel", "User logged in, but clientId is null. SignalR NOT started. Forcing logout.")
                                    authRepository.logout()
                                }
                            } else {
                                Log.w("AuthViewModel", "isLoggedIn is true, but currentTokenForSignalR is blank. SignalR NOT started. Forcing logout.")
                                authRepository.logout()
                            }
                        } else {
                            Log.d("AuthViewModel", "User not logged in. Ensuring SignalR is stopped.")
                            signalRService.stopConnection()
                        }
                    }
                } else {
                    Log.d("AuthViewModel", "Session check not yet complete. Waiting to collect isLoggedIn for SignalR.")
                }
            }
        }

        userManager.hasNewNotificationsFlow
            .distinctUntilChanged()
            .onEach { hasNew -> _hasNewNotifications.value = hasNew }
            .launchIn(viewModelScope)
    }

    // --- Funcții pentru Login ---
    fun onLoginEmailChanged(email: String) {
        _uiState.update { it.copy(emailLogin = email, emailErrorLogin = null, generalError = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordLogin = password, passwordErrorLogin = null, generalError = null) }
    }

    private fun validateLoginFields(): Boolean {
        val state = _uiState.value
        var isValid = true
        // Resetarea erorilor anterioare
        _uiState.update { it.copy(emailErrorLogin = null, passwordErrorLogin = null) }

        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        if (state.emailLogin.isBlank()) {
            _uiState.update { it.copy(emailErrorLogin = "Email cannot be empty.") }
            isValid = false
        } else if (!state.emailLogin.matches(emailRegex)) {
            _uiState.update { it.copy(emailErrorLogin = "Invalid email format.") }
            isValid = false
        }

        if (state.passwordLogin.isBlank()) {
            _uiState.update { it.copy(passwordErrorLogin = "Password cannot be empty.") }
            isValid = false
        }
        return isValid
    }

    fun login() {
        if (!validateLoginFields()) {
            Log.d("AuthViewModel", "Login validation failed on submit.")
            return
        }

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, isLoginSuccess = false) }
            val request = UserLoginRequest(state.emailLogin.trim(), state.passwordLogin)
            val loginResult = authRepository.login(request)

            loginResult.onSuccess {
                Log.i("AuthViewModel", "Login successful from ViewModel.")
                if (!_isSessionCheckComplete.value) _isSessionCheckComplete.value = true

                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId == null) {
                    _uiState.update { it.copy(isLoading = false, generalError = "Login successful but token is invalid.") }
                    authRepository.logout() // Forțează ștergerea token-ului invalid
                    return@launch
                }
                val vehiclesCheckResult = authRepository.hasVehicles(clientId)
                vehiclesCheckResult.onSuccess {
                    _uiState.update { it.copy(isLoading = false, hasVehicle = true, isLoginSuccess = true) }
                }.onFailure {
                    _uiState.update { it.copy(isLoading = false, hasVehicle = false, isLoginSuccess = true) }
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, generalError = exception.message ?: "Login failed.")
                }
                if (!_isSessionCheckComplete.value) _isSessionCheckComplete.value = true
            }
        }
    }

    // --- Funcții pentru Register ---
    fun onRegisterUsernameChanged(username: String) {
        _uiState.update { it.copy(usernameRegister = username, usernameErrorRegister = null, generalError = null) }
    }

    fun onRegisterEmailChanged(email: String) {
        _uiState.update { it.copy(emailRegister = email, emailErrorRegister = null, generalError = null) }
    }

    fun onRegisterPhoneNumberChanged(phone: String) {
        val numericPhone = phone.filter { it.isDigit() }
        _uiState.update { it.copy(phoneNumberRegister = numericPhone, phoneNumberErrorRegister = null, generalError = null) }
    }

    fun onRegisterPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordRegister = password, passwordErrorRegister = null, generalError = null) }
        updatePasswordFeedback(password)
        if (_uiState.value.confirmPasswordRegister.isNotBlank()) {
            validateConfirmPasswordRegister(password, _uiState.value.confirmPasswordRegister, forSubmit = false)
        }
    }

    fun onRegisterConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPasswordRegister = confirmPassword, confirmPasswordErrorRegister = null, generalError = null) }
        validateConfirmPasswordRegister(_uiState.value.passwordRegister, confirmPassword, forSubmit = false)
    }

    fun onRegisterTermsAcceptedChanged(accepted: Boolean) {
        _uiState.update { it.copy(termsAcceptedRegister = accepted, termsErrorRegister = null, generalError = null) }
    }

    private fun updatePasswordFeedback(password: String) {
        val requirements = mutableListOf<Pair<String, Boolean>>()
        requirements.add("Minimum 8 characters" to (password.length >= 8))
        requirements.add("Contains uppercase" to password.any { it.isUpperCase() })
        requirements.add("Contains lowercase" to password.any { it.isLowerCase() })
        requirements.add("Contains digit" to password.any { it.isDigit() })
        // Opcional: requirements.add("Contains special char" to password.any { !it.isLetterOrDigit() })

        val strengthScore = requirements.count { it.second }
        val strength = when {
            password.isEmpty() -> PasswordStrength.NONE
            strengthScore <= 2 -> PasswordStrength.WEAK // Ajustează pragurile după nevoie
            strengthScore == 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
        // Eroarea de lungime este singura afișată în timp real sub câmpul parolei
        val lengthError = if (password.isNotBlank() && password.length < 8) "Password is too short." else null

        _uiState.update {
            it.copy(
                passwordStrengthRegister = strength,
                passwordRequirementsMet = requirements,
                passwordErrorRegister = lengthError // Doar eroarea de lungime este setată aici
            )
        }
    }

    // forSubmit = true este când se apasă butonul Register
    // forSubmit = false este pentru validare în timp real (doar nepotrivire)
    private fun validateConfirmPasswordRegister(password: String, confirm: String, forSubmit: Boolean) {
        if (forSubmit && confirm.isBlank()) {
            _uiState.update { it.copy(confirmPasswordErrorRegister = "Please confirm your password.") }
        } else if (password != confirm && (forSubmit || confirm.isNotBlank())) {
            _uiState.update { it.copy(confirmPasswordErrorRegister = "Passwords do not match.") }
        } else {
            _uiState.update { it.copy(confirmPasswordErrorRegister = null) }
        }
    }

    private fun validateAllRegistrationFields(): Boolean {
        val state = _uiState.value
        var isValid = true
        // Resetează erorile înainte de a le re-evalua
        _uiState.update { it.copy(
            usernameErrorRegister = null, emailErrorRegister = null, phoneNumberErrorRegister = null,
            passwordErrorRegister = _uiState.value.passwordErrorRegister, // Păstrează eroarea de lungime dacă există
            confirmPasswordErrorRegister = _uiState.value.confirmPasswordErrorRegister, // Păstrează eroarea de nepotrivire
            termsErrorRegister = null
        )}

        if (state.usernameRegister.isBlank()) {
            _uiState.update { it.copy(usernameErrorRegister = "Username is required.") }; isValid = false
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        if (state.emailRegister.isBlank()) {
            _uiState.update { it.copy(emailErrorRegister = "Email is required.") }; isValid = false
        } else if (!state.emailRegister.matches(emailRegex)) {
            _uiState.update { it.copy(emailErrorRegister = "Invalid email format.") }; isValid = false
        }

        if (state.phoneNumberRegister.isNotBlank() && state.phoneNumberRegister.length != 10) {
            _uiState.update { it.copy(phoneNumberErrorRegister = "Phone must be 10 digits if provided.") }; isValid = false
        }

        // Validarea completă a parolei la submit
        val password = state.passwordRegister
        var passwordFullValidationError: String? = null
        if (password.length < 8) passwordFullValidationError = "Password must be at least 8 characters."
        else if (!password.any { it.isUpperCase() }) passwordFullValidationError = "Must contain an uppercase letter."
        else if (!password.any { it.isLowerCase() }) passwordFullValidationError = "Must contain a lowercase letter."
        else if (!password.any { it.isDigit() }) passwordFullValidationError = "Must contain a digit."
        // else if (!password.any { !it.isLetterOrDigit() }) passwordFullValidationError = "Must contain a special character."

        if (passwordFullValidationError != null) {
            _uiState.update { it.copy(passwordErrorRegister = passwordFullValidationError) }; isValid = false
        } else {
            // Dacă toate cerințele sunt ok, dar exista o eroare de lungime de la feedback-ul real-time, o ștergem
            if(state.passwordErrorRegister?.contains("too short") == false) { // Nu șterge dacă e alt tip de eroare
                _uiState.update { it.copy(passwordErrorRegister = null) }
            }
        }


        validateConfirmPasswordRegister(state.passwordRegister, state.confirmPasswordRegister, forSubmit = true)
        if (_uiState.value.confirmPasswordErrorRegister != null) isValid = false

        if (!state.termsAcceptedRegister) {
            _uiState.update { it.copy(termsErrorRegister = "You must accept the terms.") }; isValid = false
        }

        return isValid
    }

    fun register() {
        if (!validateAllRegistrationFields()) {
            Log.d("AuthViewModel", "Registration validation failed on submit.")
            return
        }

        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null, isRegisterSuccess = false) }
            val phoneNumberForRequest = state.phoneNumberRegister.takeIf { it.isNotBlank() } ?: "0"
            val request = UserRegisterRequest(
                username = state.usernameRegister.trim(),
                email = state.emailRegister.trim(),
                password = state.passwordRegister,
                phoneNumber = phoneNumberForRequest,
                roleId = 1
            )
            Log.d("AuthViewModel", "Attempting to register with: $request")
            val result = authRepository.register(request)
            result.onSuccess {
                Log.i("AuthViewModel", "Registration successful via repository.")
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true, successMessage = "Registration successful! Please login.") }
            }.onFailure { exception ->
                Log.e("AuthViewModel", "Registration failed: ${exception.message}")
                _uiState.update {
                    it.copy(isLoading = false, generalError = exception.message ?: "An unknown registration error occurred.")
                }
            }
        }
    }

    // --- Funcții de Reset și Clear ---
    fun resetLoginSuccessHandled() { _uiState.update { it.copy(isLoginSuccess = false, hasVehicle = false) } }
    fun resetRegisterSuccessHandled() { _uiState.update { it.copy(isRegisterSuccess = false, successMessage = null) } }
    fun clearGeneralError() { _uiState.update { it.copy(generalError = null) } }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "logout() called.")
            AuthViewModel.initialSessionCheckLogicHasRun = false
            _isSessionCheckComplete.value = false
            authRepository.logout()
            _uiState.update { AuthUiState() } // Resetează la starea inițială goală
            _hasNewNotifications.value = false
        }
    }
}