package com.example.cartrack.features.change_password

import com.example.cartrack.features.auth.PasswordStrength

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",

    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmNewPasswordError: String? = null,

    val newPasswordStrength: PasswordStrength = PasswordStrength.NONE,
    val newPasswordRequirements: List<Pair<String, Boolean>> = emptyList(),

    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)