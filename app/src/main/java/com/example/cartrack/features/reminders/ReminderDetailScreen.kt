package com.example.cartrack.features.reminders

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.ConfirmationDialog
import com.example.cartrack.features.reminders.helpers.ReminderDetailCard
import com.example.cartrack.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    navController: NavHostController,
    reminderId: Int,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(reminderId) {
        viewModel.loadReminderDetails(reminderId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderDetailEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    uiState.confirmationDialogType?.let { dialogType ->
        ConfirmationDialog(
            onDismissRequest = viewModel::dismissConfirmationDialog,
            onConfirmation = viewModel::onConfirmAction,
            dialogTitle = dialogType.title,
            dialogText = dialogType.text,
            icon = dialogType.icon,
            isLoading = uiState.isActionLoading
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.reminder?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                uiState.reminder != null -> {
                    val reminder = uiState.reminder!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        ReminderDetailCard(reminder = reminder)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { navController.navigate(Routes.editReminderRoute(reminder.configId)) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isActionLoading && reminder.isEditable
                            ) {
                                Icon(Icons.Default.Edit, "Edit")
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Edit Intervals")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (reminder.isActive) viewModel.showConfirmationDialog(ConfirmationDialogType.DeactivateReminder)
                                        else viewModel.executeToggleActiveStatus()
                                    },
                                    enabled = !uiState.isActionLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (reminder.isActive) "Set Inactive" else "Set Active")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.showConfirmationDialog(ConfirmationDialogType.RestoreToDefault) },
                                    enabled = !uiState.isActionLoading && reminder.isEditable,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                ) {
                                    Text("Restore")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}