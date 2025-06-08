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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
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
            // Încărcăm numele vehiculului (opțional, dar util pentru UI)
            // Presupunând că repo-ul are o metodă de a lua toate vehiculele

            vehicleRepository.getMaintenanceHistory(vehicleId).onSuccess { history ->
                val grouped = groupEventsByMonth(history)
                _uiState.update { it.copy(isLoading = false, groupedEvents = grouped) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun groupEventsByMonth(events: List<MaintenanceLogResponseDto>): Map<String, List<MaintenanceLogResponseDto>> {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return events
            .sortedByDescending { it.date }
            .groupBy { event ->
                try {
                    val instant = Instant.parse(event.date)
                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    localDateTime.toJavaLocalDateTime().format(formatter)
                } catch (e: Exception) {
                    "Unknown Date"
                }
            }
    }
}