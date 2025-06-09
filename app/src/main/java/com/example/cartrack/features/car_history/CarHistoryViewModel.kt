package com.example.cartrack.features.car_history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.history.MaintenanceLogResponseDto
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CarHistoryViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarHistoryState())
    val uiState: StateFlow<CarHistoryState> = _uiState.asStateFlow()

    private val vehicleId: Int = checkNotNull(savedStateHandle[Routes.CAR_HISTORY_ARG_ID])

    init {
        loadHistory()
    }

    fun loadHistory() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val vehicleNameResult = vehicleRepository.getVehiclesByClientId()
            val historyResult = vehicleRepository.getMaintenanceHistory(vehicleId)

            if (historyResult.isSuccess) {
                val vehicleName = vehicleNameResult.getOrNull()?.find { it.id == vehicleId }?.let {
                    "${it.producer} ${it.series}"
                } ?: "Vehicle History"

                val history = historyResult.getOrNull() ?: emptyList()
                val grouped = groupEventsByMonth(history)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groupedEvents = grouped,
                        vehicleName = vehicleName
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = historyResult.exceptionOrNull()?.message) }
            }
        }
    }

    private fun groupEventsByMonth(events: List<MaintenanceLogResponseDto>): List<Pair<String, List<MaintenanceLogResponseDto>>> {
        if (events.isEmpty()) {
            return emptyList()
        }

        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

        val groupedMap = events
            .sortedByDescending { it.date }
            .groupBy { event ->
                try {
                    // --- AICI ESTE CORECȚIA PRINCIPALĂ ---
                    // Vom folosi java.time.ZonedDateTime pentru a parsa un string ISO complet
                    ZonedDateTime.parse(event.date).format(formatter)
                } catch (e: DateTimeParseException) {
                    // Dacă eșuează, încercăm să parsăm doar ca dată (ex: "2025-06-09")
                    try {
                        LocalDate.parse(event.date).format(formatter)
                    } catch (e2: DateTimeParseException) {
                        "Unknown Date"
                    }
                }
            }

        return groupedMap.entries.map { it.key to it.value }
    }
}