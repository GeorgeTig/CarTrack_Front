package com.example.cartrack.features.change_password

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.auth.helpers.PasswordRequirementsList
import com.example.cartrack.features.auth.helpers.PasswordStrengthIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavHostController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var currentPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }
    var confirmPassVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.error) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        uiState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PasswordField(
                    value = uiState.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChanged,
                    label = "Current Password",
                    isVisible = currentPassVisible,
                    onVisibilityChange = { currentPassVisible = it },
                    error = uiState.currentPasswordError,
                    isEnabled = !uiState.isSaving
                )
                PasswordField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChanged,
                    label = "New Password",
                    isVisible = newPassVisible,
                    onVisibilityChange = { newPassVisible = it },
                    error = uiState.newPasswordError,
                    supportingContent = {
                        Column {
                            PasswordStrengthIndicator(strength = uiState.newPasswordStrength)
                            PasswordRequirementsList(requirements = uiState.newPasswordRequirements, passwordInput = uiState.newPassword)
                        }
                    },
                    isEnabled = !uiState.isSaving
                )
                PasswordField(
                    value = uiState.confirmNewPassword,
                    onValueChange = viewModel::onConfirmNewPasswordChanged,
                    label = "Confirm New Password",
                    isVisible = confirmPassVisible,
                    onVisibilityChange = { confirmPassVisible = it },
                    error = uiState.confirmNewPasswordError,
                    isEnabled = !uiState.isSaving
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveNewPassword() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Save Password")
                }
            }

            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                        .clickable(enabled = false, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    error: String?,
    isEnabled: Boolean,
    supportingContent: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { onVisibilityChange(!isVisible) }) {
                Icon(if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle visibility")
            }
        },
        supportingText = {
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            else supportingContent?.invoke()
        }
    )
}