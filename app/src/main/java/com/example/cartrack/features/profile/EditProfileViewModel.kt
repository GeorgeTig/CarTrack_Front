package com.example.cartrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            userRepository.getUserInfo().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        username = user.username,
                        phoneNumber = if (user.phoneNumber == "0") "" else user.phoneNumber
                    )
                }
            }.onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, usernameError = if (newUsername.isBlank()) "Username cannot be empty" else null) }
    }

    fun onPhoneNumberChanged(newPhoneNumber: String) {
        val digits = newPhoneNumber.filter { it.isDigit() }
        _uiState.update { it.copy(
            phoneNumber = digits,
            phoneNumberError = if (digits.isNotEmpty() && digits.length < 10) "Must be at least 10 digits" else null
        )}
    }

    fun saveChanges() {
        val state = _uiState.value
        if (state.usernameError != null || state.phoneNumberError != null || state.username.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            userRepository.updateProfile(state.username, state.phoneNumber).onSuccess {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            }.onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }
}