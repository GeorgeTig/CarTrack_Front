package com.example.cartrack.feature.auth.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.auth.presentation.helpers.PasswordRequirementsList
import com.example.cartrack.feature.auth.presentation.helpers.PasswordStrengthIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    navigateBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage, uiState.generalError) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetRegisterSuccessHandled()
            onRegisterSuccess()
        }
        uiState.generalError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearGeneralError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = navigateBackToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Login")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Username
            OutlinedTextField(
                value = uiState.usernameRegister,
                onValueChange = viewModel::onRegisterUsernameChanged,
                label = { Text("Username*") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.PersonOutline, null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                isError = uiState.usernameErrorRegister != null,
                supportingText = { uiState.usernameErrorRegister?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Email
            OutlinedTextField(
                value = uiState.emailRegister,
                onValueChange = viewModel::onRegisterEmailChanged,
                label = { Text("Email Address*") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                isError = uiState.emailErrorRegister != null,
                supportingText = { uiState.emailErrorRegister?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Phone Number
            OutlinedTextField(
                value = uiState.phoneNumberRegister,
                onValueChange = viewModel::onRegisterPhoneNumberChanged,
                label = { Text("Phone Number (10 digits if entered)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                isError = uiState.phoneNumberErrorRegister != null,
                supportingText = { uiState.phoneNumberErrorRegister?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Password
            OutlinedTextField(
                value = uiState.passwordRegister,
                onValueChange = viewModel::onRegisterPasswordChanged,
                label = { Text("Password*") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.LockOpen, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, if (passwordVisible) "Hide" else "Show")
                    }
                },
                singleLine = true,
                isError = uiState.passwordErrorRegister != null,
                supportingText = {
                    Column { // Column pentru a afișa eroarea și feedback-ul parolei
                        uiState.passwordErrorRegister?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        PasswordStrengthIndicator(strength = uiState.passwordStrengthRegister)
                        PasswordRequirementsList(requirements = uiState.passwordRequirementsMet)
                    }
                }
            )

            // Confirm Password
            OutlinedTextField(
                value = uiState.confirmPasswordRegister,
                onValueChange = viewModel::onRegisterConfirmPasswordChanged,
                label = { Text("Confirm Password*") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.register()
                }),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, if (confirmPasswordVisible) "Hide" else "Show")
                    }
                },
                singleLine = true,
                isError = uiState.confirmPasswordErrorRegister != null,
                supportingText = { uiState.confirmPasswordErrorRegister?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            // Terms and Conditions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { viewModel.onRegisterTermsAcceptedChanged(!uiState.termsAcceptedRegister) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.termsAcceptedRegister,
                    onCheckedChange = { viewModel.onRegisterTermsAcceptedChanged(it) }
                    // Poți adăuga colors la Checkbox dacă vrei să personalizezi
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "I accept the Terms and Conditions.", // Placeholder
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (uiState.termsErrorRegister != null) {
                Text(
                    uiState.termsErrorRegister!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp) // Aliniere cu textul, nu cu checkbox-ul
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.register()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Create Account", style = MaterialTheme.typography.labelLarge)
                }
            }

            TextButton(onClick = navigateBackToLogin, enabled = !uiState.isLoading, modifier = Modifier.padding(top = 8.dp)) {
                Text("Already have an account? Login", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
