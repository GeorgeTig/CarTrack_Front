package com.example.cartrack.feature.auth.presentation

import android.util.Log // Adaugă acest import dacă lipsește
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.signalr.SignalRService
import com.example.cartrack.core.storage.TokenManager // Adaugă import pentru TokenManager
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import com.example.cartrack.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull // Adaugă acest import
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val jwtDecoder: JwtDecoder,
    private val signalRService: SignalRService,
    private val userManager: UserManager,
    private val tokenManager: TokenManager // Injectează TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hasVehicles: StateFlow<Boolean> = authRepository.hasVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _hasNewNotifications = MutableStateFlow(false)
    val hasNewNotifications: StateFlow<Boolean> = _hasNewNotifications.asStateFlow()

    init {
        userManager.hasNewNotificationsFlow
            .distinctUntilChanged()
            .onEach { hasNew ->
                _hasNewNotifications.value = hasNew
            }
            .launchIn(viewModelScope)

        // Monitorizează starea de login pentru a porni/opri SignalR corespunzător
        viewModelScope.launch {
            isLoggedIn.collect { loggedInStatus ->
                if (loggedInStatus) {
                    // Verifică existența token-ului și a clientID-ului înainte de a porni SignalR
                    // Acest lucru este important mai ales la pornirea aplicației când utilizatorul este deja logat.
                    val token = tokenManager.tokenFlow.firstOrNull()
                    if (!token.isNullOrBlank()) {
                        val clientId = jwtDecoder.getClientIdFromToken() // Re-decode pentru siguranță
                        if (clientId != null) {
                            Log.d("AuthViewModel", "User is logged in (init or state change). Attempting to start SignalR.")
                            signalRService.startConnection()
                        } else {
                            Log.w("AuthViewModel", "User is logged in, but clientId could not be retrieved from token. SignalR not started. Token might be invalid.")
                            // Consideră un mecanism de logout aici dacă token-ul este invalid dar isLoggedIn este true
                        }
                    } else {
                        Log.w("AuthViewModel", "isLoggedIn is true, but token is missing or blank. SignalR not started.")
                        // Poate fi un caz de eroare, token-ul ar trebui să existe dacă isLoggedIn este true
                    }
                } else {
                    Log.d("AuthViewModel", "User is not logged in (init or state change). Ensuring SignalR is stopped.")
                    signalRService.stopConnection()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isLoginSuccess = false) }

            val request = UserLoginRequest(email.trim(), password)
            val result1 = authRepository.login(request)

            result1.onSuccess {
                // `isLoggedIn` va deveni true, iar colectorul din `init` va gestiona pornirea SignalR.
                // Nu mai este nevoie să apelăm signalRService.startConnection() explicit aici,
                // pentru a evita apeluri multiple și pentru a centraliza logica.
                // Totuși, ne asigurăm că `clientId` este disponibil pentru `hasVehicles`.
                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid client token after login.") }
                    // Opțional, oprește SignalR aici dacă a apucat să pornească și clientId e null.
                    // Dar colectorul din init ar trebui să se ocupe și de oprire dacă token-ul devine invalid.
                    // signalRService.stopConnection()
                    return@launch
                }
                // signalRService.startConnection() // <- Eliminat de aici, gestionat de colectorul din init

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
                // Asigură oprirea SignalR la eșecul login-ului, deși colectorul din init ar trebui s-o facă.
                signalRService.stopConnection()
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

    fun register(username: String, email: String, password: String, phoneNumber: String) {
        validatePassword(password)
        validateEmail(email)
        validateUsername(username)
        validatePhoneNumber(phoneNumber)

        if (_uiState.value.phoneNumberError != null || _uiState.value.emailError != null || _uiState.value.usernameError != null || _uiState.value.passwordError != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isRegisterSuccess = false) }

            val request = UserRegisterRequest(
                username = username.trim(),
                email = email.trim(),
                password = password,
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
                        error = exception.message ?: "An unknown registration error occurred."
                    )
                }
            }
        }
    }

    fun resetLoginSuccessHandled() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    fun resetRegisterSuccessHandled() {
        _uiState.update { it.copy(isRegisterSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch {
            // Oprirea SignalR este gestionată de colectorul din `init` când `isLoggedIn` devine false.
            // signalRService.stopConnection() // <- Eliminat de aici
            authRepository.logout() // Acest lucru va face ca `isLoggedIn` să emită `false`
            _uiState.update { AuthUiState() }
            _hasNewNotifications.value = false
        }
    }
}