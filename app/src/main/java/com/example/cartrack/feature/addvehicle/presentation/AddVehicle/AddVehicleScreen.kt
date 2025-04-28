package com.example.cartrack.feature.addvehicle.presentation.AddVehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVinDecoded: (results: List<VinDecodedResponseDto>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Effect for showing general errors
    val currentError = uiState.error
    LaunchedEffect(currentError) {
        currentError?.let { errorMsg ->
            focusManager.clearFocus()
            snackbarHostState.showSnackbar(message = errorMsg, duration = SnackbarDuration.Long)
            viewModel.errorShown()
        }
    }

    // Effect to trigger navigation when decode results are available
    val currentDecodeResult = uiState.decodeResult
    LaunchedEffect(currentDecodeResult) {
        currentDecodeResult?.let { results ->
             if (results.isNotEmpty()) {
                focusManager.clearFocus()
                onVinDecoded(results)
                viewModel.clearDecodeResult()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Informational Icon and Text
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Enter your vehicle's 17-character VIN to automatically fetch its details.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // VIN Input TextField
            OutlinedTextField(
                value = uiState.vinInput,
                onValueChange = viewModel::onVinInputChange,
                label = { Text("VIN Number") },
                placeholder = { Text("e.g., 1HG...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions (
                    onDone = {
                        focusManager.clearFocus()
                        if (uiState.vinInput.length == 17) {
                            viewModel.decodeVin()
                        }
                    }
                ),
                isError = uiState.vinValidationError != null,
                supportingText = {

                    AnimatedVisibility(visible = uiState.vinValidationError != null) {
                        val currentValidationError = uiState.vinValidationError
                        if (currentValidationError != null) {
                            Text(currentValidationError, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },

                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.decodeVin()
                },
                // Enable only when VIN is valid length, no error, and not loading
                enabled = !uiState.isLoading && uiState.vinInput.length == 17 && uiState.vinValidationError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Find Vehicle Details", style = MaterialTheme.typography.titleMedium)
                }
            }

        }
    }
}