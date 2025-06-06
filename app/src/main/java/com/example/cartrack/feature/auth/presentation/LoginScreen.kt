package com.example.cartrack.feature.auth.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onLoginSuccessNavigateToMain: () -> Unit, // Redenumit pentru claritate (are vehicule)
    onLoginSuccessNavigateToAddVehicle: () -> Unit, // Redenumit (nu are vehicule)
    navigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }

    // Gestionează navigarea la succes
    LaunchedEffect(uiState.isLoginSuccess, uiState.hasVehicle) {
        if (uiState.isLoginSuccess) {
            focusManager.clearFocus()
            if (uiState.hasVehicle) {
                onLoginSuccessNavigateToMain()
            } else {
                onLoginSuccessNavigateToAddVehicle()
            }
            viewModel.resetLoginSuccessHandled() // Resetează flag-ul
        }
    }

    // Afișează eroarea generală
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearGeneralError() // Resetează eroarea
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
        // Nu avem SnackbarHost aici, folosim Toast pentru erori generale
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("Login to continue", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.emailLogin,
                onValueChange = viewModel::onLoginEmailChanged, // Apel ViewModel
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = uiState.emailErrorLogin != null, // Eroare specifică login-ului
                supportingText = {
                    uiState.emailErrorLogin?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                value = uiState.passwordLogin,
                onValueChange = viewModel::onLoginPasswordChanged, // Apel ViewModel
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (!uiState.isLoading) viewModel.login() // Apel ViewModel fără parametri
                }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, if (passwordVisible) "Hide" else "Show")
                    }
                },
                singleLine = true,
                isError = uiState.passwordErrorLogin != null, // Eroare specifică login-ului
                supportingText = {
                    uiState.passwordErrorLogin?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (!uiState.isLoading) viewModel.login() // Apel ViewModel fără parametri
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Login", style = MaterialTheme.typography.labelLarge)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = navigateToRegister, enabled = !uiState.isLoading) {
                    Text("Register Now", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}