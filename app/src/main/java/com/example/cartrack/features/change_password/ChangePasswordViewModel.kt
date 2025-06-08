package com.example.cartrack.features.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.domain.repository.UserRepository
import com.example.cartrack.features.auth.PasswordStrength
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordState())
    val uiState: StateFlow<ChangePasswordState> = _uiState.asStateFlow()

    fun onCurrentPasswordChanged(password: String) { _uiState.update { it.copy(currentPassword = password, currentPasswordError = null) } }
    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null) }
        updatePasswordFeedback(password)
    }
    fun onConfirmNewPasswordChanged(password: String) { _uiState.update { it.copy(confirmNewPassword = password, confirmNewPasswordError = null) } }

    private fun updatePasswordFeedback(password: String) {
        val requirements = listOf(
            "Minimum 8 characters" to (password.length >= 8),
            "Contains an uppercase letter" to password.any { it.isUpperCase() },
            "Contains a lowercase letter" to password.any { it.isLowerCase() },
            "Contains a digit" to password.any { it.isDigit() }
        )
        val strength = when (requirements.count { it.second }) {
            0, 1, 2 -> if (password.isEmpty()) PasswordStrength.NONE else PasswordStrength.WEAK
            3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
        _uiState.update { it.copy(newPasswordStrength = strength, newPasswordRequirements = requirements) }
    }

    private fun validate(): Boolean {
        val state = _uiState.value
        _uiState.update { it.copy(currentPasswordError = null, newPasswordError = null, confirmNewPasswordError = null) }
        var isValid = true

        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(currentPasswordError = "Current password is required.") }; isValid = false
        }
        if (!state.newPasswordRequirements.all { it.second }) {
            _uiState.update { it.copy(newPasswordError = "New password does not meet all requirements.") }; isValid = false
        }
        if (state.newPassword != state.confirmNewPassword) {
            _uiState.update { it.copy(confirmNewPasswordError = "Passwords do not match.") }; isValid = false
        }
        return isValid
    }

    fun saveNewPassword() {
        if (!validate()) return

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val request = ChangePasswordRequestDto(
                currentPassword = _uiState.value.currentPassword,
                newPassword = _uiState.value.newPassword
            )
            userRepository.changePassword(request).onSuccess {
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}