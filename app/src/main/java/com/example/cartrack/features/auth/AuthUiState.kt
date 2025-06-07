package com.example.cartrack.features.auth

enum class PasswordStrength {
    NONE, WEAK, MEDIUM, STRONG
}

data class AuthUiState(
    // Stări generale
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null,

    // Stări specifice pentru Login
    val emailLogin: String = "",
    val passwordLogin: String = "",
    val emailErrorLogin: String? = null,
    val passwordErrorLogin: String? = null,
    val isLoginSuccess: Boolean = false,
    val requiresVehicleAddition: Boolean = false,

    // Stări specifice pentru Register
    val usernameRegister: String = "",
    val emailRegister: String = "",
    val phoneNumberRegister: String = "",
    val passwordRegister: String = "",
    val confirmPasswordRegister: String = "",
    val termsAcceptedRegister: Boolean = false,
    val isRegisterSuccess: Boolean = false,

    val usernameErrorRegister: String? = null,
    val emailErrorRegister: String? = null,
    val phoneNumberErrorRegister: String? = null,
    val passwordErrorRegister: String? = null,
    val confirmPasswordErrorRegister: String? = null,
    val termsErrorRegister: String? = null,

    val passwordStrengthRegister: PasswordStrength = PasswordStrength.NONE,
    val passwordRequirementsMet: List<Pair<String, Boolean>> = emptyList()
)