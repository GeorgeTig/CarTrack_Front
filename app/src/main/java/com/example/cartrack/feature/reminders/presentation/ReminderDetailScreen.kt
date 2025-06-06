package com.example.cartrack.feature.reminders.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderDetailCard
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Edit
import com.example.cartrack.core.ui.components.ConfirmationDialog
import com.example.cartrack.feature.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    navController: NavHostController,
    reminderId: Int,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = reminderId) {
        viewModel.loadReminderDetails(reminderId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderDetailEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is ReminderDetailEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    // --- Aici se afișează dialogul de confirmare, dacă este cazul ---
    uiState.confirmationDialogType?.let { dialogType ->
        ConfirmationDialog(
            onDismissRequest = { viewModel.dismissConfirmationDialog() },
            onConfirmation = { viewModel.onConfirmAction() },
            dialogTitle = dialogType.title,
            dialogText = dialogType.text,
            icon = dialogType.icon,
            isLoading = uiState.isActionLoading
        )
    }
    // NOTĂ: Am eliminat dialogul de editare de aici, deoarece acum se navighează la un ecran nou.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.reminder?.name ?: "Reminder Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                else -> {
                    val currentReminder = uiState.reminder
                    if (currentReminder != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ReminderDetailCard(reminder = currentReminder)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { navController.navigate(Routes.editReminderRoute(currentReminder.configId)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isActionLoading && currentReminder.isEditable
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                    Text("Edit Intervals")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (currentReminder.isActive) {
                                                viewModel.showConfirmationDialog(ConfirmationDialogType.DeactivateReminder)
                                            } else {
                                                // Activarea nu necesită confirmare, este o acțiune pozitivă
                                                viewModel.executeToggleActiveStatus()
                                            }
                                        },
                                        enabled = !uiState.isActionLoading,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (currentReminder.isActive) "Set Inactive" else "Set Active")
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.showConfirmationDialog(ConfirmationDialogType.RestoreToDefault) },
                                        enabled = !uiState.isActionLoading && currentReminder.isEditable,
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
}