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

    private var initialUsername: String = ""
    private var initialPhoneNumber: String = ""

    init {
        loadInitialUserData()
    }

    private fun loadInitialUserData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            userRepository.getUserInfo().onSuccess { user ->
                val formattedPhoneNumber = if (user.phoneNumber == "0" || user.phoneNumber.isBlank()) "" else user.phoneNumber
                initialUsername = user.username
                initialPhoneNumber = formattedPhoneNumber
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        username = user.username,
                        phoneNumber = formattedPhoneNumber
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, usernameError = null) }
    }

    fun onPhoneNumberChanged(newPhoneNumber: String) {
        val digitsOnly = newPhoneNumber.filter { it.isDigit() }
        _uiState.update { it.copy(phoneNumber = digitsOnly, phoneNumberError = null) }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        _uiState.update { it.copy(usernameError = null, phoneNumberError = null, error = null) }
        var isValid = true

        if (state.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username cannot be empty.") }
            isValid = false
        }
        if (state.phoneNumber.isNotEmpty() && state.phoneNumber.length < 10) {
            _uiState.update { it.copy(phoneNumberError = "Phone must be at least 10 digits if provided.") }
            isValid = false
        }
        if (state.username.trim() == initialUsername && state.phoneNumber.trim() == initialPhoneNumber) {
            _uiState.update { it.copy(error = "No changes were made.") }
            isValid = false
        }
        return isValid
    }

    fun saveChanges() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val state = _uiState.value
            userRepository.updateProfile(state.username, state.phoneNumber)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }.onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }
}