package com.example.cartrack.feature.addvehicle.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.addvehicle.presentation.components.StepIndicator
import kotlinx.coroutines.launch
// Import step composables AND the new indicator
import com.example.cartrack.feature.addvehicle.presentation.steps.*

// Import helpers if moved (ensure you have these)
// import com.example.cartrack.feature.addvehicle.presentation.components.DropdownSelection
// import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVehicleAddedSuccessfully: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    // Removed context and coroutineScope as they weren't used directly here

    // --- LaunchedEffects for errors and success (no change) ---
    LaunchedEffect(uiState.error) { /* ... */ }
    LaunchedEffect(uiState.isSaveSuccess) { /* ... */ }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                // Title can now show the full step name from the helper
                title = { Text(getStepTitle(uiState.currentStep)) },
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
        },
        bottomBar = { // --- Bottom Bar for Navigation (no change) ---
            BottomAppBar( containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = {
                        focusManager.clearFocus() // Clear focus when navigating back
                        viewModel.goToPreviousStep() // <-- THE MISSING CALL
                    },
                    enabled = uiState.isPreviousEnabled && !uiState.isLoading, // Use state flag
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) { Text("Previous") }
                Spacer(Modifier.width(8.dp)) // Add spacer if buttons are side-by-side
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.currentStep == AddVehicleStep.CONFIRM) {
                            viewModel.saveVehicle() // Directly call save on Confirm step
                        } else {
                            viewModel.goToNextStep()
                        }
                    },
                    // Enable Save button only on Confirm step and when not loading
                    enabled = (uiState.currentStep == AddVehicleStep.CONFIRM && !uiState.isLoading) || (uiState.currentStep != AddVehicleStep.CONFIRM && uiState.isNextEnabled && !uiState.isLoading),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp) // Ensure it takes space if needed
                ) {
                    // Logic for button text/icon (Save vs Next)
                    when {
                        uiState.isLoading && uiState.currentStep == AddVehicleStep.CONFIRM -> CircularProgressIndicator(/*...*/)
                        uiState.currentStep == AddVehicleStep.CONFIRM -> {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Save Vehicle")
                        }

                        else -> Text("Next")
                    }
                }
            }
        }
    ) { paddingValues ->
        // --- Main Content Area ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply scaffold padding
                .padding(horizontal = 16.dp) // Add horizontal padding for content
                .verticalScroll(rememberScrollState())
        ) {
            // --- ADD THE STEP INDICATOR ---
            StepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            // --- Animated Content for Step Details ---
            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier.fillMaxWidth(), // Ensure content fills width
                // --- CORRECTED transitionSpec ---
                transitionSpec = {
                    // Define slide direction based on step order
                    val slideDirection = if (targetState.ordinal > initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Start // Slide in from right
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.End   // Slide in from left
                    }

                    slideIntoContainer(slideDirection) + fadeIn() togetherWith
                            slideOutOfContainer(slideDirection) + fadeOut() using
                            SizeTransform(clip = false) // Allows content to change size smoothly
                },
                // ------------------------------------
                label = "StepContentAnimation"
            ) { targetStep ->
                // --- Loading Overlay ---
                if (uiState.isLoading && uiState.currentStep == AddVehicleStep.VIN && targetStep == AddVehicleStep.VIN) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Decoding VIN...")
                    }
                } else {
                    // --- Render Specific Step Content ---
                    when (targetStep) {
                        AddVehicleStep.VIN -> VinInputStepContent(
                            uiState,
                            viewModel::onVinInputChange,
                            viewModel::decodeVinAndProceed
                        )

                        AddVehicleStep.SERIES -> SeriesStepContent(
                            uiState,
                            viewModel::selectProducer,
                            viewModel::selectSeries
                        )

                        AddVehicleStep.ENGINE -> EngineStepContent(uiState, viewModel::selectEngine)
                        AddVehicleStep.BODY -> BodyStepContent(uiState, viewModel::selectBody)
                        // Pass selectModel correctly now
                        AddVehicleStep.MILEAGE -> MileageStepContent(
                            uiState,
                            viewModel::onMileageChange,
                            viewModel::selectModel
                        )

                        AddVehicleStep.CONFIRM -> ConfirmStepContent(uiState)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

    }
}

// Helper function to get full step title (Keep or move)
private fun getStepTitle(step: AddVehicleStep): String {
    return when (step) {
        AddVehicleStep.VIN -> "Step 1: Enter VIN"
        AddVehicleStep.SERIES -> "Step 2: Select Series"
        AddVehicleStep.ENGINE -> "Step 3: Select Engine"
        AddVehicleStep.BODY -> "Step 4: Select Body Style"
        AddVehicleStep.MILEAGE -> "Step 5: Enter Mileage"
        AddVehicleStep.CONFIRM -> "Step 6: Confirm & Save"
    }
}