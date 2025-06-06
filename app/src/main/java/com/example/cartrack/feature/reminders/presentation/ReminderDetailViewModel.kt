package com.example.cartrack.feature.reminders.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.vehicle.data.model.ReminderRequestDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository
import com.example.cartrack.feature.maintenance.presentation.EditReminderFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderDetailState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ReminderDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val logTag = "ReminderDetailVM"

    fun loadReminderDetails(reminderId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val vehicleId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            if (vehicleId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Could not identify current vehicle.") }
                return@launch
            }

            // Ideal, ai avea un endpoint GET /api/reminders/{reminderId}
            // Ca workaround, luăm toate reminderele pentru vehicul și îl găsim pe cel specific
            val result = vehicleRepository.getRemindersByVehicleId(vehicleId)
            result.onSuccess { reminders ->
                val specificReminder = reminders.find { it.configId == reminderId }
                if (specificReminder != null) {
                    _uiState.update { it.copy(isLoading = false, reminder = specificReminder) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Reminder not found.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load details.") }
            }
        }
    }

    fun toggleReminderActiveStatus() {
        val reminder = _uiState.value.reminder ?: return
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderActiveStatus(reminder.configId)
            result.onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Status updated!"))
                // După ce statusul s-a schimbat, navigăm înapoi la lista principală care se va reîmprospăta
                _eventFlow.emit(ReminderDetailEvent.NavigateBack)
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    // --- Funcții pentru dialogul de editare ---

    fun showEditReminderDialog() {
        val reminder = _uiState.value.reminder ?: return // Nicio acțiune dacă nu există reminder
        if (!reminder.isEditable) {
            viewModelScope.launch {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("This reminder cannot be edited."))
            }
            return
        }

        _uiState.update {
            it.copy(
                isEditDialogVisible = true,
                editFormState = EditReminderFormState(
                    reminderToEdit = reminder,
                    nameInput = reminder.name, // Pre-populează numele
                    mileageIntervalInput = reminder.mileageInterval?.toString() ?: "",
                    timeIntervalInput = reminder.timeInterval.toString()
                )
            )
        }
    }

    fun dismissEditReminderDialog() {
        _uiState.update { it.copy(isEditDialogVisible = false, editFormState = EditReminderFormState()) }
    }

    // Funcția care lipsea
    fun onEditNameChanged(name: String) {
        _uiState.update {
            it.copy(
                editFormState = it.editFormState.copy(
                    nameInput = name,
                    nameError = if (name.isBlank()) "Name cannot be empty" else null
                )
            )
        }
    }

    fun onEditMileageIntervalChanged(mileage: String) {
        val digits = mileage.filter { it.isDigit() }
        val isValid = digits.isEmpty() || (digits.toIntOrNull()?.let { it >= 0 } == true)
        _uiState.update {
            it.copy(editFormState = it.editFormState.copy(
                mileageIntervalInput = digits,
                mileageIntervalError = if (!isValid) "Invalid mileage (>= 0 or empty)" else null
            ))
        }
    }

    fun onEditTimeIntervalChanged(time: String) {
        val digits = time.filter { it.isDigit() }
        val isValid = digits.toIntOrNull()?.let { it > 0 } == true
        _uiState.update {
            it.copy(editFormState = it.editFormState.copy(
                timeIntervalInput = digits,
                timeIntervalError = if (!isValid) "Time interval must be > 0 days" else null
            ))
        }
    }

    fun saveReminderEdits() {
        // Corecția pentru smart cast: creăm o variabilă locală imutabilă
        val currentReminder = _uiState.value.reminder ?: return
        val form = _uiState.value.editFormState

        // ... (logica de validare pentru save) ...
        var hasError = false
        if (form.timeIntervalInput.toIntOrNull() == null || form.timeIntervalInput.toInt() <= 0) {
            hasError = true
            // setează eroarea în state dacă vrei s-o afișezi
        }
        if(hasError) return

        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            val request = ReminderRequestDto(
                id = currentReminder.configId, // Folosim variabila locală
                mileageInterval = form.mileageIntervalInput.toIntOrNull() ?: 0,
                timeInterval = form.timeIntervalInput.toInt() // Asigură-te că nu e null aici
            )
            val result = vehicleRepository.updateReminder(request)
            result.onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Reminder updated!"))
                dismissEditReminderDialog()
                loadReminderDetails(currentReminder.configId) // Reîncarcă detaliile
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }

    fun restoreReminderToDefaults() {
        val currentReminder = _uiState.value.reminder ?: return
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            val result = vehicleRepository.updateReminderToDefault(currentReminder.configId)
            result.onSuccess {
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Restored to defaults!"))
                dismissEditReminderDialog()
                loadReminderDetails(currentReminder.configId)
            }.onFailure { e ->
                _eventFlow.emit(ReminderDetailEvent.ShowMessage("Error: ${e.message}"))
            }
            _uiState.update { it.copy(isActionLoading = false) }
        }
    }
}