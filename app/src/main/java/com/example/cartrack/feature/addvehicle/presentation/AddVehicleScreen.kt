package com.example.cartrack.feature.addvehicle.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.addvehicle.presentation.components.StepIndicator
import com.example.cartrack.feature.addvehicle.presentation.steps.*

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

    // --- Effects for Snackbar and Navigation ---
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Short
            )
        }
    }
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onVehicleAddedSuccessfully()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(getStepTitle(uiState.currentStep)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Previous Button
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.goToPreviousStep()
                    },
                    enabled = uiState.isPreviousEnabled && !uiState.isLoading,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    border = BorderStroke(1.dp, if (uiState.isPreviousEnabled && !uiState.isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant) // Theme border
                ) {
                    Text("Previous")
                }

                // Next / Save Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.currentStep == AddVehicleStep.CONFIRM) {
                            viewModel.saveVehicle()
                        } else {
                            viewModel.goToNextStep()
                        }
                    },
                    enabled = (uiState.currentStep == AddVehicleStep.CONFIRM && !uiState.isLoading) || (uiState.currentStep != AddVehicleStep.CONFIRM && uiState.isNextEnabled && !uiState.isLoading),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    // Button Content: Loading / Save / Next
                    when {
                        uiState.isLoading && uiState.currentStep == AddVehicleStep.CONFIRM -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        // Save button content
                        uiState.currentStep == AddVehicleStep.CONFIRM -> {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Save Vehicle")
                        }
                        // Next button content
                        else -> {
                            Text("Next")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Step Indicator using themed component
            StepIndicator(
                currentStep = uiState.currentStep,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            // Animated Content Switcher for Steps
            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    val slideDirection = if (targetState.ordinal > initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    // Combine slide with fade
                    slideIntoContainer(slideDirection) + fadeIn() togetherWith
                            slideOutOfContainer(slideDirection) + fadeOut() using
                            SizeTransform(clip = false)
                },
                label = "StepContentAnimation"
            ) { targetStep ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    if (uiState.isLoading && targetStep == AddVehicleStep.VIN && uiState.vinInput.length == 17) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top=32.dp)) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Decoding VIN...", color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        when (targetStep) {
                            AddVehicleStep.VIN -> VinInputStepContent(uiState = uiState, onVinChange = viewModel::onVinInputChange)
                            AddVehicleStep.SERIES -> SeriesStepContent(uiState = uiState, onSelectProducer = viewModel::selectProducer, onSelectSeries = viewModel::selectSeries)
                            AddVehicleStep.ENGINE -> EngineStepContent(uiState = uiState, onSelectEngine = viewModel::selectEngine)
                            AddVehicleStep.BODY -> BodyStepContent(uiState = uiState, onSelectBody = viewModel::selectBody)
                            AddVehicleStep.MILEAGE -> MileageStepContent(uiState = uiState, onMileageChange = viewModel::onMileageChange, onSelectModel = viewModel::selectModel)
                            AddVehicleStep.CONFIRM -> ConfirmStepContent(uiState = uiState)
                        }
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