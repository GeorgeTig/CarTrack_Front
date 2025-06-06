package com.example.cartrack.feature.editprofile.presentation

import com.example.cartrack.feature.profile.data.model.UserResponseDto

data class EditProfileState(
    val isLoading: Boolean = true,
    val user: UserResponseDto? = null,

    // Câmpurile formularului
    val username: String = "",
    val phoneNumber: String = "",

    // Erorile formularului
    val usernameError: String? = null,
    val phoneNumberError: String? = null,

    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)