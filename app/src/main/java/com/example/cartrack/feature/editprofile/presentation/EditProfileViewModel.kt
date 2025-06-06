package com.example.cartrack.feature.editprofile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.utils.JwtDecoder
import com.example.cartrack.feature.profile.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val jwtDecoder: JwtDecoder
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val userId = jwtDecoder.getClientIdFromToken()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not identified.") }
                return@launch
            }

            userRepository.getUserInfo(userId).onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        username = user.username,
                        phoneNumber = if (user.phoneNumber == "0" || user.phoneNumber.isBlank()) "" else user.phoneNumber
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update {
            it.copy(
                username = newUsername,
                usernameError = if (newUsername.isBlank()) "Username cannot be empty" else null
            )
        }
    }

    fun onPhoneNumberChanged(newPhoneNumber: String) {
        val digitsOnly = newPhoneNumber.filter { it.isDigit() }
        _uiState.update {
            it.copy(
                phoneNumber = digitsOnly,
                phoneNumberError = if (digitsOnly.isNotEmpty() && digitsOnly.length != 10) "Must be 10 digits if provided" else null
            )
        }
    }

    fun saveChanges() {
        val currentState = _uiState.value
        if (currentState.usernameError != null || currentState.phoneNumberError != null || currentState.username.isBlank()) {
            Log.w("EditProfileVM", "Save prevented by validation errors.")
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            userRepository.updateProfile(currentState.username, currentState.phoneNumber)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }
}