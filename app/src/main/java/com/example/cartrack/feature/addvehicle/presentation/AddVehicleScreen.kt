package com.example.cartrack.feature.addvehicle.presentation
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions // Correct KeyboardActions import
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSave: (uniqueVehicleData: VinDecodedResponseDto) -> Unit,
    onNavigateToSelection: (ambiguousResults: List<VinDecodedResponseDto>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // *** FIX 1: Smart Casting for Error Snackbar ***
    val currentError = uiState.error // Create local copy
    LaunchedEffect(currentError) { // Effect based on local copy
        currentError?.let { errorMsg -> // Smart cast works on local copy
            snackbarHostState.showSnackbar(message = errorMsg, duration = SnackbarDuration.Short)
            viewModel.errorShown()
        }
    }
    // *** END FIX 1 ***

    // *** FIX 2: Smart Casting for Decode Result Handling ***
    val currentDecodeResult = uiState.decodeResult // Create local copy
    LaunchedEffect(currentDecodeResult) { // Effect based on local copy
        currentDecodeResult?.let { results -> // Smart cast works on local copy
            if (results.isNotEmpty()) {
                // TODO: Implement analysis and navigation logic
                val isUnique = results.size == 1 &&
                        results[0].vehicleModelInfo.size == 1 &&
                        results[0].vehicleModelInfo[0].engineInfo.size == 1 &&
                        results[0].vehicleModelInfo[0].bodyInfo.size == 1

                if (isUnique) {
                    Toast.makeText(context, "Unique vehicle found! (Save TBD)", Toast.LENGTH_SHORT).show()
                    // onNavigateToSave(results[0])
                } else {
                    Toast.makeText(context, "Multiple options found. (Selection TBD)", Toast.LENGTH_SHORT).show()
                    // onNavigateToSelection(results)
                }
            }
        }
    }
    // *** END FIX 2 ***


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle by VIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter the 17-character Vehicle Identification Number (VIN).",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.vinInput,
                onValueChange = viewModel::onVinInputChange,
                label = { Text("VIN Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrect = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions ( // Use correct import: androidx.compose.foundation.text.KeyboardActions
                    onDone = {
                        focusManager.clearFocus()
                        if (uiState.vinInput.length == 17) {
                            viewModel.decodeVin()
                        }
                    }
                ),
                // *** FIX 3: Smart Casting for Validation Error Text ***
                isError = uiState.vinValidationError != null, // Check for null is fine
                supportingText = {
                    val currentValidationError = uiState.vinValidationError // Create local copy
                    if (currentValidationError != null) { // Smart cast works on local copy
                        Text(currentValidationError, color = MaterialTheme.colorScheme.error)
                    }
                },
                // *** END FIX 3 ***
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.decodeVin()
                },
                enabled = !uiState.isLoading && uiState.vinInput.length == 17 && uiState.vinValidationError == null,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Decode VIN")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder Area (uses fixed currentDecodeResult)
            if (currentDecodeResult != null && !uiState.isLoading) {
                Text(
                    "Decode Result Received (Placeholder):",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${currentDecodeResult.size} potential match(es) found.", // Use local copy here too
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}