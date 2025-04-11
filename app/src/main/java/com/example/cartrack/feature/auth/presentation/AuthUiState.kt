package com.example.cartrack.feature.auth.presentation

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null, // Holds user-friendly error messages
    val isLoginSuccess: Boolean = false, // Flag to trigger navigation after successful login
    val isRegisterSuccess: Boolean = false, // Flag to trigger navigation/feedback after registration
)