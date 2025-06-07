package com.example.cartrack.features.profile

data class EditProfileState(
    val isLoading: Boolean = true,
    val username: String = "",
    val phoneNumber: String = "",
    val usernameError: String? = null,
    val phoneNumberError: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)