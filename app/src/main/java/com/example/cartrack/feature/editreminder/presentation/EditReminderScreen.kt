package com.example.cartrack.feature.editreminder.presentation

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.feature.editreminder.presentation.helpers.IntervalsEditForm
import com.example.cartrack.feature.editreminder.presentation.helpers.ReminderHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    navController: NavHostController,
    reminderId: Int,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // LaunchedEffects rămân la fel
    LaunchedEffect(key1 = reminderId) {
        viewModel.loadInitialData(reminderId)
    }
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is EditReminderEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is EditReminderEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Reminder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
        // Am eliminat complet FloatingActionButton
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
                uiState.reminder != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Antetul vizual
                        Spacer(modifier = Modifier.height(24.dp))
                        ReminderHeader(
                            name = uiState.reminder?.name ?: "...",
                            typeId = uiState.reminder?.typeId
                        )

                        // Divider sub antet
                        Divider(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp))

                        // Formularul de editare (care acum are subtitluri)
                        IntervalsEditForm(
                            mileageInterval = uiState.mileageIntervalInput,
                            onMileageChange = viewModel::onMileageIntervalChanged,
                            mileageError = uiState.mileageIntervalError,
                            timeInterval = uiState.timeIntervalInput,
                            onTimeChange = viewModel::onTimeIntervalChanged,
                            timeError = uiState.timeIntervalError
                        )

                        // Butonul de salvare, mutat aici
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = { viewModel.saveChanges() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth() // Ocupă toată lățimea containerului cu padding
                                .height(50.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Save")
                                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Save Changes")
                            }
                        }

                        // Spacer la final pentru a nu fi lipit de marginea de jos
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}