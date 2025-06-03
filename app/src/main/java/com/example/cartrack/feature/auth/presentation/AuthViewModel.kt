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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    // Flag pentru a ne asigura că verificarea inițială a sesiunii se face o singură dată
    companion object { // Folosim un companion object pentru un flag la nivel de clasă (dacă ViewModel e recreat des)
        // Sau un simplu var dacă ViewModel e un singleton efectiv sau ActivityRetained.
        // Pentru HiltViewModel, un var membru e suficient.
        @Volatile private var initialSessionCheckLogicHasRun = false
    }


    init {
        Log.d("AuthViewModel", "AuthViewModel instance created/init called. initialSessionCheckLogicHasRun: $initialSessionCheckLogicHasRun")

        // Logica pentru verificarea inițială a sesiunii și silent refresh
        if (!initialSessionCheckLogicHasRun) {
            initialSessionCheckLogicHasRun = true // Setează flag-ul pentru a preveni rularea multiplă
            Log.d("AuthViewModel", "INIT - Performing initial session check (first run for this app lifecycle or ViewModel instance).")
            viewModelScope.launch {
                val initialToken = tokenManager.accessTokenFlow.firstOrNull()
                if (!initialToken.isNullOrBlank()) {
                    Log.d("AuthViewModel", "INIT - Initial token found. Attempting silent refresh.")
                    val refreshResult = authRepository.attemptSilentRefresh()
                    if (refreshResult.isSuccess) {
                        Log.i("AuthViewModel", "INIT - Silent refresh successful.")
                    } else {
                        Log.w("AuthViewModel", "INIT - Silent refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.d("AuthViewModel", "INIT - No initial token. Skipping silent refresh.")
                }
                _isSessionCheckComplete.value = true
                Log.d("AuthViewModel", "INIT - Session check complete flag set to true.")
            }
        } else {
            // Dacă logica de refresh a rulat deja, doar ne asigurăm că _isSessionCheckComplete este true
            // pentru ca NavHost să poată naviga corect dacă ViewModel-ul a fost recreat.
            // Acest caz e mai puțin probabil să fie necesar dacă ViewModel-ul are scope corect.
            if (!_isSessionCheckComplete.value) {
                _isSessionCheckComplete.value = true
                Log.d("AuthViewModel", "INIT - Session check was already run, ensuring flag is true.")
            }
            Log.d("AuthViewModel", "INIT - Initial session check logic already run for this app lifecycle or ViewModel instance.")
        }

        // Colectorul pentru isLoggedIn și SignalR - rulează mereu pentru a reacționa la schimbări
        viewModelScope.launch {
            // Așteaptă ca _isSessionCheckComplete să fie true înainte de a colecta isLoggedIn pentru SignalR
            _isSessionCheckComplete.collectLatest { sessionCheckDone ->
                if (sessionCheckDone) {
                    Log.d("AuthViewModel", "Session check is complete. Starting to collect isLoggedIn for SignalR.")
                    isLoggedIn.collectLatest { loggedInStatus -> // collectLatest pentru a anula colectarea veche dacă isLoggedIn se schimbă rapid
                        Log.d("AuthViewModel", "isLoggedIn state for SignalR: $loggedInStatus")
                        if (loggedInStatus) {
                            val currentTokenForSignalR = tokenManager.accessTokenFlow.firstOrNull()
                            if (!currentTokenForSignalR.isNullOrBlank()) {
                                val clientId = jwtDecoder.getClientIdFromToken()
                                if (clientId != null) {
                                    Log.d("AuthViewModel", "User is logged in (clientId: $clientId). Starting SignalR.")
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
                            Log.d("AuthViewModel", "User is not logged in. Ensuring SignalR is stopped.")
                            signalRService.stopConnection()
                        }
                    }
                } else {
                    Log.d("AuthViewModel", "Session check not yet complete. Waiting to collect isLoggedIn for SignalR.")
                }
            }
        }

        // Colector pentru notificări
        userManager.hasNewNotificationsFlow
            .distinctUntilChanged()
            .onEach { hasNew -> _hasNewNotifications.value = hasNew }
            .launchIn(viewModelScope)
    }

    // ... restul metodelor (login, register, logout, etc.) ...
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isLoginSuccess = false) }
            val request = UserLoginRequest(email.trim(), password)
            val loginResult = authRepository.login(request)

            loginResult.onSuccess {
                Log.i("AuthViewModel", "Login successful from ViewModel.")
                // isLoggedIn se va actualiza, SignalR va porni prin colectorul din init
                // Setează session check complete la true după un login reușit, dacă nu era deja.
                if (!_isSessionCheckComplete.value) _isSessionCheckComplete.value = true

                val clientId = jwtDecoder.getClientIdFromToken()
                if (clientId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Login successful but token is invalid.") }
                    authRepository.logout()
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
                    it.copy(isLoading = false, error = exception.message ?: "Login failed.")
                }
                if (!_isSessionCheckComplete.value) _isSessionCheckComplete.value = true // Și la eșec, verificarea s-a terminat
            }
        }
    }

    fun register(username: String, email: String, password: String, phoneNumber: String) {
        validatePassword(password)
        validateEmail(email)
        validateUsername(username)
        validatePhoneNumber(phoneNumber)

        val currentState = _uiState.value
        if (currentState.usernameError != null || currentState.emailError != null ||
            currentState.passwordError != null || currentState.phoneNumberError != null) {
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
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true, success = "Registration successful! Please login.") }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, error = exception.message ?: "Registration failed.")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "logout() called.")
            // Setează flag-ul la false pentru ca la următoarea pornire a aplicației (după ce e omorâtă complet)
            // sau la crearea unei noi instanțe de ViewModel (dacă e cazul), verificarea să se refacă.
            // Acest lucru e relevant mai ales dacă ViewModel-ul este distrus și recreat des.
            // Dacă ViewModel-ul e un singleton efectiv pentru durata de viață a aplicației,
            // acest reset e mai puțin critic aici, dar nu strică.
            initialSessionCheckLogicHasRun = false // Reset flag on logout
            _isSessionCheckComplete.value = false // Resetăm și acest flag pentru a forța ecranul de loading la următorul start

            authRepository.logout()
            _uiState.update { AuthUiState() }
            _hasNewNotifications.value = false
        }
    }

    fun validateEmail(email: String) {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        _uiState.update { it.copy(emailError = if (!email.matches(emailRegex)) "Invalid email." else null) }
    }

    fun validateUsername(username: String) {
        _uiState.update { it.copy(usernameError = if (username.isBlank()) "Username is required." else null) }
    }

    fun validatePassword(password: String) {
        _uiState.update { it.copy(passwordError = if (password.length < 8) "Password too short." else null) }
    }

    fun validatePhoneNumber(phoneNumber: String) {
        val phoneRegex = "^\\+?[0-9]{10,15}$".toRegex()
        if (phoneNumber.isNotBlank() && !phoneNumber.matches(phoneRegex)) {
            _uiState.update { it.copy(phoneNumberError = "Invalid phone number.") }
        } else {
            _uiState.update { it.copy(phoneNumberError = null) }
        }
    }

    fun resetLoginSuccessHandled() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    fun resetRegisterSuccessHandled() {
        _uiState.update { it.copy(isRegisterSuccess = false, success = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}