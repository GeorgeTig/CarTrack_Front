package com.example.cartrack.features.reminders

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.getIconForMaintenanceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    navController: NavHostController,
    reminderId: Int,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(reminderId) {
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
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
            } else if (uiState.reminder != null) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val reminder = uiState.reminder!!
                    Icon(getIconForMaintenanceType(reminder.typeId), "Type", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(reminder.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Divider(Modifier.padding(vertical = 24.dp))

                    OutlinedTextField(
                        value = uiState.mileageIntervalInput,
                        onValueChange = viewModel::onMileageIntervalChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mileage Interval (miles)") },
                        placeholder = { Text("Leave blank if not applicable") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        isError = uiState.mileageIntervalError != null,
                        supportingText = { uiState.mileageIntervalError?.let { Text(it) } },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.timeIntervalInput,
                        onValueChange = viewModel::onTimeIntervalChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Time Interval (days)*") },
                        placeholder = { Text("e.g., 180") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        isError = uiState.timeIntervalError != null,
                        supportingText = { uiState.timeIntervalError?.let { Text(it) } },
                        singleLine = true
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = viewModel::saveChanges,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, "Save")
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}