package com.example.cartrack.feature.editprofile.presentation.helper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Un Composable care conține câmpurile de formular pentru editarea profilului.
 *
 * @param username Valoarea curentă pentru username.
 * @param onUsernameChange Callback la schimbarea username-ului.
 * @param usernameError Eroarea de validare pentru username.
 * @param phoneNumber Valoarea curentă pentru numărul de telefon.
 * @param onPhoneNumberChange Callback la schimbarea numărului de telefon.
 * @param phoneNumberError Eroarea de validare pentru numărul de telefon.
 */
@Composable
fun EditProfileForm(
    username: String,
    onUsernameChange: (String) -> Unit,
    usernameError: String?,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    phoneNumberError: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Secțiunea Username ---
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username*") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") },
            singleLine = true,
            isError = usernameError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            supportingText = {
                usernameError?.let { Text(it) }
            },
            shape = MaterialTheme.shapes.medium
        )

        // --- Secțiunea Phone Number ---
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = phoneNumberError != null,
            supportingText = {
                phoneNumberError?.let { Text(it) }
            },
            shape = MaterialTheme.shapes.medium
        )
    }
}