package com.example.cartrack.feature.profile.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.graphics.vector.ImageVector


sealed class ProfileConfirmationDialog(
    val title: String,
    val text: String,
    val icon: ImageVector
) {
    object Logout : ProfileConfirmationDialog(
        title = "Log Out?",
        text = "Are you sure you want to log out from your account?",
        icon = Icons.AutoMirrored.Filled.ExitToApp
    )
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userInfo: com.example.cartrack.feature.profile.data.model.UserResponseDto? = null,
    val vehicles: List<com.example.cartrack.core.vehicle.data.model.VehicleResponseDto> = emptyList(),
    val activeVehicleId: Int? = null, // NOU: ID-ul vehiculului activ
    val error: String? = null,
    val dialogType: ProfileConfirmationDialog? = null // NOU: Pentru dialogul de logout
)