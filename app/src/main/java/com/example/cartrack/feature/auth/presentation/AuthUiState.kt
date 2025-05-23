package com.example.cartrack.feature.auth.presentation

data class AuthUiState(
    val isLoading: Boolean = false, // General error messages for Snackbar
    val isLoginSuccess: Boolean = false,
    val hasVehicle: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val success: String? = null,

    // Specific error messages for input fields of register
    val error: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneNumberError: String? = null
)