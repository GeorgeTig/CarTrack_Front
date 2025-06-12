package com.example.cartrack.features.add_custom_reminder

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.features.add_vehicle.components.DropdownSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomReminderScreen(
    navController: NavHostController,
    viewModel: AddCustomReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is AddCustomReminderEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                is AddCustomReminderEvent.NavigateBack -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_reminders", true)
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Custom Reminder") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!uiState.isSaving) viewModel.saveCustomReminder() }
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                } else {
                    Icon(Icons.Default.Save, "Save Reminder")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Reminder Name*") },
                            placeholder = { Text("e.g., Check first aid kit") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                            isError = uiState.nameError != null,
                            supportingText = { uiState.nameError?.let { Text(it) } },
                            singleLine = true
                        )

                        DropdownSelection(
                            label = "Reminder Type*",
                            options = uiState.availableTypes,
                            selectedOption = uiState.availableTypes.find { it.id == uiState.selectedTypeId },
                            onOptionSelected = { viewModel.onTypeSelected(it.id) },
                            optionToString = { it.name },
                            isError = uiState.typeError != null,
                            errorText = uiState.typeError
                        )

                        Text("Set at least one interval below. Leave blank if not applicable.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = uiState.mileageInterval,
                            onValueChange = viewModel::onMileageChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mileage Interval (mi)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.dateInterval,
                            onValueChange = viewModel::onDateChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Time Interval (days)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            singleLine = true
                        )

                        uiState.intervalError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}