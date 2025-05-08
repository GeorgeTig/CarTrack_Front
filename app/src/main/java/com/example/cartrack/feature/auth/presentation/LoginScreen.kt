package com.example.cartrack.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess1: () -> Unit,
    onLoginSuccess2: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState.isLoading
    val snackbarError = uiState.error

    val snackbarHostState = remember { SnackbarHostState() }

    // Validation function
    fun validateFields(): Boolean {
        emailError = if (email.isBlank()) { "Email cannot be empty." }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Please enter a valid email address." }
        else { null }

        passwordError = if (password.isBlank()) { "Password cannot be empty." }
        else { null }
        return emailError == null && passwordError == null
    }

    // --- Effects ---
    LaunchedEffect(uiState.isLoginSuccess, uiState.hasVehicle) {
        if (uiState.isLoginSuccess) {
            focusManager.clearFocus()
            if (uiState.hasVehicle) onLoginSuccess1() else onLoginSuccess2()
            viewModel.resetLoginSuccessHandled()
        }
    }
    LaunchedEffect(snackbarError) {
        if (snackbarError != null) {
            focusManager.clearFocus()
            snackbarHostState.showSnackbar(message = snackbarError, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    // --- UI ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Make scrollable
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically) // Spacing + Vertical Center
        ) {
            // Optional Icon
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Header Text
            Text(
                "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Login to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Email Field with Icon
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) { Text(emailError ?: "", color = MaterialTheme.colorScheme.error) }
                },
                enabled = !isLoading
            )

            // Password Field with Icon and Visibility Toggle
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (validateFields() && !isLoading) { viewModel.login(email, password) }
                }),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                singleLine = true,
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) { Text(passwordError ?: "", color = MaterialTheme.colorScheme.error) }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Login Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (validateFields() && !isLoading) { viewModel.login(email, password) }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Navigation to Register Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = navigateToRegister,
                    enabled = !isLoading
                ) {
                    Text(
                        "Register Now",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}