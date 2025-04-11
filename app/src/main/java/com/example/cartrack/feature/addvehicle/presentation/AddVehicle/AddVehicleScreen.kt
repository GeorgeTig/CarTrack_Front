package com.example.cartrack.feature.addvehicle.presentation.AddVehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // For TextField Icon
import androidx.compose.material.icons.filled.Info // For info icon
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
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Keep this import
import com.example.cartrack.feature.addvehicle.presentation.AddVehicle.AddVehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    // Updated callback: Navigate when VIN is decoded, passing the results
    onVinDecoded: (results: List<VinDecodedResponseDto>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Effect for showing general errors
    val currentError = uiState.error // Create local copy for smart casting
    LaunchedEffect(currentError) {
        currentError?.let { errorMsg ->
            focusManager.clearFocus() // Clear focus when showing error
            snackbarHostState.showSnackbar(message = errorMsg, duration = SnackbarDuration.Long)
            viewModel.errorShown() // Notify VM that error was shown
        }
    }

    // Effect to trigger navigation when decode results are available
    val currentDecodeResult = uiState.decodeResult // Create local copy
    LaunchedEffect(currentDecodeResult) {
        currentDecodeResult?.let { results ->
            // Only navigate if there are results (empty list case handled by VM error state)
            if (results.isNotEmpty()) {
                focusManager.clearFocus() // Clear focus before navigating
                onVinDecoded(results) // Call the navigation lambda passed from NavHost
                viewModel.clearDecodeResult() // Reset decodeResult in VM immediately
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
                .padding(horizontal = 24.dp, vertical = 32.dp) // Adjusted padding
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Informational Icon and Text
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null, // Decorative
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
                placeholder = { Text("e.g., 1HG...") }, // Example placeholder
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters, // Auto-caps VIN
                    autoCorrect = false,
                    imeAction = ImeAction.Done // Keyboard action button
                ),
                keyboardActions = KeyboardActions ( // Handle keyboard action
                    onDone = {
                        focusManager.clearFocus() // Dismiss keyboard
                        // Trigger decode only if length is correct
                        if (uiState.vinInput.length == 17) {
                            viewModel.decodeVin()
                        }
                    }
                ),
                isError = uiState.vinValidationError != null, // Show error outline
                supportingText = {
                    // Animate visibility of the validation error message
                    AnimatedVisibility(visible = uiState.vinValidationError != null) {
                        val currentValidationError = uiState.vinValidationError // Local copy for smart cast
                        if (currentValidationError != null) {
                            Text(currentValidationError, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },

                enabled = !uiState.isLoading, // Disable while loading
                shape = MaterialTheme.shapes.medium // Consistent shape
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Decode Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.decodeVin()
                },
                // Enable only when VIN is valid length, no error, and not loading
                enabled = !uiState.isLoading && uiState.vinInput.length == 17 && uiState.vinValidationError == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // Slightly larger button
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp) // Subtle lift
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary, // Contrast on button
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Find Vehicle Details", style = MaterialTheme.typography.titleMedium)
                }
            }
            // No need to show results here anymore, navigation handles it
        } // End Column
    } // End Scaffold
} // End AddVehicleScreen