package com.example.cartrack.feature.addvehicle.presentation

import android.util.Log
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.feature.addvehicle.presentation.components.StepIndicator
import com.example.cartrack.feature.addvehicle.presentation.steps.*
import com.example.cartrack.feature.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddVehicleScreen(
    navController: NavHostController,
    fromLoginNoVehicles: Boolean,
    viewModel: AddVehicleViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onVehicleAddedSuccessfully: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // --- LOGICA DE NAVIGARE NOUĂ ---

    val exitAddVehicleFlow: () -> Unit = { // Specificăm explicit tipul pentru a fi siguri
        if (fromLoginNoVehicles) {
            Log.d("AddVehicleScreen", "Exit flow (fromLogin=true). Logging out.")
            authViewModel.logout() // Se apelează funcția, dar valoarea ei nu se returnează din lambda
        } else {
            Log.d("AddVehicleScreen", "Exit flow (normal). Popping back stack.")
            navController.popBackStack()
        }
    }

    // Gestionează butonul fizic "back" al telefonului
    BackHandler(enabled = true) {
        if (uiState.currentStep == AddVehicleStep.VIN) {
            // Dacă suntem la primul pas, butonul back fizic iese din flux
            exitAddVehicleFlow()
        } else {
            // Altfel, merge la pasul anterior
            viewModel.goToPreviousStep()
        }
    }

    // --- Efecte pentru UI (rămân la fel) ---
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            viewModel.resetSaveStatus()
            onVehicleAddedSuccessfully()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(getStepTitle(uiState.currentStep, fromLoginNoVehicles)) },
                navigationIcon = {
                    // Butonul stânga-sus ACUM va ieși mereu din flux
                    IconButton(onClick = exitAddVehicleFlow) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Add Vehicle")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            )
        },
        bottomBar = {
            val isLoadingOverall = uiState.isLoadingVinDetails || uiState.isLoadingNextStep || uiState.isSaving
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Butonul "Previous" ACUM va merge doar la pasul anterior
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.goToPreviousStep()
                    },
                    enabled = uiState.isPreviousEnabled && !isLoadingOverall,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    border = BorderStroke(1.dp, if (uiState.isPreviousEnabled && !isLoadingOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) { Text("Previous") }

                // Butonul "Next" rămâne cu logica lui
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (uiState.currentStep == AddVehicleStep.CONFIRM) viewModel.saveVehicle()
                        else viewModel.goToNextStep()
                    },
                    enabled = if (uiState.currentStep == AddVehicleStep.CONFIRM) !uiState.isSaving else (uiState.isNextEnabled && !isLoadingOverall),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    val showButtonSpinner = (uiState.isLoadingNextStep && uiState.currentStep != AddVehicleStep.VIN) ||
                            (uiState.isLoadingVinDetails && uiState.currentStep == AddVehicleStep.VIN) ||
                            (uiState.isSaving && uiState.currentStep == AddVehicleStep.CONFIRM)

                    if (showButtonSpinner) {
                        CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else if (uiState.currentStep == AddVehicleStep.CONFIRM) {
                        Icon(Icons.Filled.Check, null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Save Vehicle")
                    } else Text("Next")
                }
            }
        }
    ) { paddingValues ->
        // Restul UI-ului (Column, StepIndicator, AnimatedContent etc.) rămâne exact la fel
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
                            isLoading = uiState.isLoadingVinDetails,
                            onSkipToManual = viewModel::userClickedSkipVinOrEnterManually
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
            Spacer(Modifier.height(72.dp))
        }

        if (uiState.isSaving) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
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
        AddVehicleStep.VIN -> "Step 1: VIN or Skip"
        AddVehicleStep.SERIES_YEAR -> "Step 2: Series & Year"
        AddVehicleStep.ENGINE_DETAILS -> "Step 3: Engine Details"
        AddVehicleStep.BODY_DETAILS -> "Step 4: Body Details"
        AddVehicleStep.VEHICLE_INFO -> "Step 5: Vehicle Info"
        AddVehicleStep.CONFIRM -> "Step 6: Confirm & Save"
    }
}