package com.example.cartrack.features.add_vehicle

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.add_vehicle.components.StepIndicator
import com.example.cartrack.features.add_vehicle.steps.*
import com.example.cartrack.features.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddVehicleScreen(
    navController: NavHostController,
    fromLoginNoVehicles: Boolean,
    onVehicleAddedSuccessfully: () -> Unit,
    viewModel: AddVehicleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val exitAddVehicleFlow: () -> Unit = {
        if (fromLoginNoVehicles) {
            // Dacă utilizatorul iese din fluxul obligatoriu, îl delogăm.
            authViewModel.logout()
        } else {
            navController.popBackStack()
        }
    }

    BackHandler(enabled = true) {
        if (uiState.currentStep == AddVehicleStep.VIN) {
            exitAddVehicleFlow()
        } else {
            viewModel.goToPreviousStep()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onVehicleAddedSuccessfully()
            viewModel.resetSaveStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(getStepTitle(uiState.currentStep, fromLoginNoVehicles)) },
                navigationIcon = {
                    IconButton(onClick = exitAddVehicleFlow) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Add Vehicle")
                    }
                }
            )
        },
        bottomBar = {
            val isLoadingOverall = uiState.isLoadingVinDetails || uiState.isLoadingNextStep || uiState.isSaving
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { focusManager.clearFocus(); viewModel.goToPreviousStep() },
                    enabled = uiState.isPreviousEnabled && !isLoadingOverall,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    border = BorderStroke(1.dp, if (uiState.isPreviousEnabled && !isLoadingOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Previous")
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.currentStep == AddVehicleStep.CONFIRM) viewModel.saveVehicle()
                        else viewModel.goToNextStep()
                    },
                    enabled = uiState.isNextEnabled && !isLoadingOverall,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    val showSpinner = (uiState.isLoadingNextStep && uiState.currentStep != AddVehicleStep.VIN) ||
                            (uiState.isLoadingVinDetails && uiState.currentStep == AddVehicleStep.VIN) ||
                            (uiState.isSaving && uiState.currentStep == AddVehicleStep.CONFIRM)

                    if (showSpinner) {
                        CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else if (uiState.currentStep == AddVehicleStep.CONFIRM) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Save Vehicle")
                    } else {
                        Text("Next")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            StepIndicator(
                currentStepOrdinal = uiState.currentStep.ordinal,
                totalSteps = uiState.totalSteps,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                    slideIntoContainer(direction) + fadeIn() togetherWith slideOutOfContainer(direction) + fadeOut() using SizeTransform(clip = false)
                },
                label = "AddVehicleStepContent"
            ) { targetStep ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    when (targetStep) {
                        AddVehicleStep.VIN -> VinInputStep(
                            vin = uiState.vinInput,
                            onVinChange = viewModel::onVinInputChange,
                            vinError = uiState.vinValidationError,
                            isLoading = uiState.isLoadingVinDetails
                        )
                        AddVehicleStep.SERIES_YEAR -> SeriesYearStep(
                            uiState = uiState,
                            onSeriesAndYearSelected = viewModel::selectSeriesAndYear,
                            isLoading = uiState.isLoadingNextStep
                        )
                        AddVehicleStep.ENGINE_DETAILS -> EngineDetailsStep(
                            uiState = uiState,
                            onSizeSelected = viewModel::selectEngineSize,
                            onTypeSelected = viewModel::selectEngineType,
                            onTransmissionSelected = viewModel::selectTransmission,
                            onDriveTypeSelected = viewModel::selectDriveType,
                            isLoading = uiState.isLoadingNextStep
                        )
                        AddVehicleStep.BODY_DETAILS -> BodyDetailsStep(
                            uiState = uiState,
                            onBodyTypeSelected = viewModel::selectBodyType,
                            onDoorNumberSelected = viewModel::selectDoorNumber,
                            onSeatNumberSelected = viewModel::selectSeatNumber,
                            isLoading = uiState.isLoadingNextStep
                        )
                        AddVehicleStep.VEHICLE_INFO -> VehicleInfoStep(
                            mileage = uiState.mileageInput,
                            onMileageChange = viewModel::onMileageChange,
                            mileageError = uiState.mileageValidationError,
                            isLoadingNextStep = uiState.isLoadingNextStep
                        )
                        AddVehicleStep.CONFIRM -> ConfirmVehicleStep(uiState = uiState)
                    }
                }
            }
            Spacer(Modifier.height(72.dp)) // Spațiu pentru BottomBar
        }

        if (uiState.isSaving) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)).clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saving Vehicle...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

private fun getStepTitle(step: AddVehicleStep, fromLoginNoVehicles: Boolean): String {
    if (fromLoginNoVehicles && step == AddVehicleStep.VIN) return "Add Your First Vehicle"
    return when (step) {
        AddVehicleStep.VIN -> "Step 1: Enter VIN"
        AddVehicleStep.SERIES_YEAR -> "Step 2: Series & Year"
        AddVehicleStep.ENGINE_DETAILS -> "Step 3: Engine Details"
        AddVehicleStep.BODY_DETAILS -> "Step 4: Body Details"
        AddVehicleStep.VEHICLE_INFO -> "Step 5: Vehicle Info"
        AddVehicleStep.CONFIRM -> "Step 6: Confirm & Save"
    }
}