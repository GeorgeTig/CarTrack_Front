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
import com.example.cartrack.core.ui.cards.ReminderCard.EditReminderDialog
import com.example.cartrack.core.ui.cards.ReminderCard.ReminderDetailCard
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Edit
import com.example.cartrack.feature.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    navController: NavHostController,
    reminderId: Int, // Primește ID-ul din argumentul de navigare
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // La prima intrare, încarcă detaliile reminder-ului
    LaunchedEffect(key1 = reminderId) {
        viewModel.loadReminderDetails(reminderId)
    }

    // Ascultă evenimentele trimise de ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderDetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ReminderDetailEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // Afișează dialogul de editare dacă este vizibil
    if (uiState.isEditDialogVisible) {
        EditReminderDialog(
            formState = uiState.editFormState,
            onDismiss = { viewModel.dismissEditReminderDialog() },
            onNameChange = viewModel::onEditNameChanged,
            onMileageIntervalChange = viewModel::onEditMileageIntervalChanged,
            onTimeIntervalChange = viewModel::onEditTimeIntervalChanged,
            onSave = { viewModel.saveReminderEdits() },
            onRestoreDefaults = { viewModel.restoreReminderToDefaults() }
        )
    }

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
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    // --- AICI ESTE CORECȚIA PRINCIPALĂ ---
                    // Creăm o variabilă locală imutabilă pentru a evita eroarea de smart cast.
                    val currentReminder = uiState.reminder
                    if (currentReminder != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Cardul cu detalii rămâne la fel
                            ReminderDetailCard(reminder = currentReminder)

                            // --- NOU: Secțiune de Acțiuni ---
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Butonul principal pentru Editare (navighează)
                                Button(
                                    onClick = {
                                        // Navighează la noul ecran de editare
                                        navController.navigate(Routes.editReminderRoute(currentReminder.configId))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isActionLoading && currentReminder.isEditable
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                                    Text("Edit Intervals")
                                }

                                // Butoane secundare (cu contur)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Buton pentru a seta starea Activ/Inactiv
                                    OutlinedButton(
                                        onClick = { viewModel.toggleReminderActiveStatus() },
                                        enabled = !uiState.isActionLoading,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (currentReminder.isActive) "Set Inactive" else "Set Active")
                                    }

                                    // Buton pentru a restaura la valorile default
                                    OutlinedButton(
                                        onClick = { viewModel.restoreReminderToDefaults() },
                                        enabled = !uiState.isActionLoading && currentReminder.isEditable,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        ),
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