package com.example.cartrack.features.edit_reminder

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
import com.example.cartrack.features.edit_reminder.helpers.EditReminderHeader
import com.example.cartrack.features.edit_reminder.helpers.IntervalsEditForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    navController: NavHostController,
    viewModel: EditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                uiState.reminder != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EditReminderHeader(
                            name = uiState.reminder?.name ?: "...",
                            typeId = uiState.reminder?.typeId
                        )
                        Divider(modifier = Modifier.padding(vertical = 24.dp))
                        IntervalsEditForm(
                            mileageInterval = uiState.mileageIntervalInput,
                            onMileageChange = viewModel::onMileageIntervalChanged,
                            mileageError = uiState.mileageIntervalError,
                            timeInterval = uiState.timeIntervalInput,
                            onTimeChange = viewModel::onTimeIntervalChanged,
                            timeError = uiState.timeIntervalError,
                            isEnabled = !uiState.isSaving
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = viewModel::saveChanges,
                            enabled = !uiState.isSaving,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
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
}