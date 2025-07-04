package com.example.cartrack.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto
import com.example.cartrack.core.data.repository.SessionCacheRepository
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager,
    private val sessionCache: SessionCacheRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<HomeEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var fetchDetailsJob: Job? = null
    private val logTag = "HomeViewModel"
    private var currentlySelectedVehicleId: Int? = null

    // Variabilă pentru a urmări timpul ultimului refresh
    private var lastRefreshTime: Long = 0

    init {
        viewModelScope.launch {
            sessionCache.vehicles
                .filterNotNull()
                .collect { vehicles ->
                    if (_uiState.value.isLoading) {
                        processVehicleList(vehicles)
                    }
                }
        }
    }

    // Funcție publică apelată din UI când ecranul devine activ
    fun onScreenResumed() {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        // Facem refresh automat doar dacă a trecut mai mult de 5 minute
        // sau dacă nu avem niciun vehicul încărcat
        val shouldRefresh = (currentTime - lastRefreshTime > 5.minutes.inWholeMilliseconds) || uiState.value.vehicles.isEmpty()

        if (shouldRefresh) {
            loadVehicles(forceRefresh = true)
        }
    }

    fun loadVehicles(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            if (_uiState.value.vehicles.isEmpty() && sessionCache.vehicles.value != null) {
                viewModelScope.launch {
                    processVehicleList(sessionCache.vehicles.value!!)
                }
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = vehicleRepository.getVehiclesByClientId()

            result.onSuccess { newVehicles ->
                // Actualizăm timpul ultimului refresh la succes
                lastRefreshTime = Clock.System.now().toEpochMilliseconds()
                sessionCache.setVehicles(newVehicles)
                processVehicleList(newVehicles)

            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun processVehicleList(vehicles: List<VehicleResponseDto>) {
        if (vehicles.isEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    vehicles = emptyList(),
                    selectedVehicle = null,
                    selectedVehicleInfo = null,
                    warnings = emptyList(),
                    dailyUsage = emptyList()
                )
            }
        } else {
            val lastUsedId = vehicleManager.lastVehicleIdFlow.firstOrNull()
            val vehicleToSelect = vehicles.find { it.id == lastUsedId } ?: vehicles.first()
            onVehicleSelected(vehicleToSelect.id, vehicles)
        }
    }

    fun onVehicleSelected(vehicleId: Int, currentVehicleList: List<VehicleResponseDto>) {
        if (vehicleId == currentlySelectedVehicleId && !_uiState.value.isLoading) return

        val vehicle = currentVehicleList.find { it.id == vehicleId } ?: return
        currentlySelectedVehicleId = vehicle.id

        _uiState.update {
            it.copy(
                isLoading = false,
                vehicles = currentVehicleList,
                selectedVehicle = vehicle
            )
        }

        viewModelScope.launch { vehicleManager.saveLastVehicleId(vehicle.id) }
        fetchSelectedVehicleDetails(vehicle.id)
    }

    private fun fetchSelectedVehicleDetails(vehicleId: Int) {
        fetchDetailsJob?.cancel()
        _uiState.update {
            it.copy(
                isLoadingDetails = true,
                warnings = emptyList(),
                dailyUsage = emptyList()
            )
        }

        fetchDetailsJob = viewModelScope.launch {
            try {
                val infoDeferred = async { vehicleRepository.getVehicleInfo(vehicleId) }
                val remindersDeferred =
                    async { vehicleRepository.getRemindersByVehicleId(vehicleId) }
                val usageDeferred = async {
                    val timeZoneId = TimeZone.currentSystemDefault().id
                    vehicleRepository.getDailyUsage(vehicleId, timeZoneId)
                }

                val infoResult = infoDeferred.await()
                val remindersResult = remindersDeferred.await()
                val usageResult = usageDeferred.await()

                _uiState.update { currentState ->
                    val warnings = remindersResult.getOrNull()
                        ?.filter { it.isActive && it.statusId in setOf(2, 3) }
                        ?.sortedByDescending { it.statusId }
                        ?: currentState.warnings

                    currentState.copy(
                        selectedVehicleInfo = infoResult.getOrNull(),
                        lastSyncTime = infoResult.getOrNull()
                            ?.let { formatLastSyncTime(it.lastUpdate) } ?: "never",
                        warnings = warnings,
                        dailyUsage = usageResult.getOrNull() ?: currentState.dailyUsage,
                        isLoadingDetails = false
                    )
                }
            } catch (e: CancellationException) {
                Log.d(logTag, "Details fetch for vehicle $vehicleId was cancelled.")
            } catch (e: Exception) {
                Log.e(logTag, "An unexpected error occurred during details fetch", e)
                _uiState.update {
                    it.copy(
                        isLoadingDetails = false,
                        error = "Failed to load vehicle details."
                    )
                }
            }
        }
    }

    fun syncMileage(newMileageString: String) {
        // --- MODIFICARE AICI ---
        // Am eliminat validarea 'newMileageValue <= currentMileage'
        // Deoarece acum este gestionată în dialog.

        val newMileageValue = newMileageString.toDoubleOrNull()
        val vehicleId = currentlySelectedVehicleId

        if (newMileageValue == null) {
            // Păstrăm o validare minimă în caz că se trimite un string gol, etc.
            viewModelScope.launch { _eventFlow.emit(HomeEvent.ShowToast("Invalid mileage value.")) }
            return
        }

        // După validare, dialogul se închide, deci nu mai e nevoie de 'dismiss' aici.
        // Apelul la 'dismissSyncMileageDialog' este făcut acum de UI (în 'onConfirm').
        // Dar îl vom adăuga totuși în 'onConfirm' pentru siguranță.
        dismissSyncMileageDialog()

        if (vehicleId == null) {
            Log.e(logTag, "Invalid vehicle ID for sync. ID: $vehicleId")
            return
        }

        viewModelScope.launch {
            val result = vehicleRepository.addMileageReading(vehicleId, newMileageValue)
            if (result.isSuccess) {
                _eventFlow.emit(HomeEvent.ShowToast("Mileage updated!"))
                fetchSelectedVehicleDetails(vehicleId)
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = if (exception is ClientRequestException) {
                    runCatching {
                        val errorResponse = exception.response.bodyAsText()
                        errorResponse.substringAfter("\"message\":\"").substringBefore("\"")
                    }.getOrDefault(exception.message)
                } else {
                    exception?.message ?: "An unknown error occurred."
                }
                _eventFlow.emit(HomeEvent.ShowToast("Error: $errorMessage"))
            }
        }
    }

    private fun formatLastSyncTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "never"
        return try {
            val instant = Instant.parse(isoString)
            val now = Clock.System.now()
            val duration = now - instant

            when {
                duration.inWholeMinutes < 2 -> "just now"
                duration.inWholeHours < 1 -> "${duration.inWholeMinutes} min ago"
                duration.inWholeDays < 1 -> "${duration.inWholeHours}h ago"
                duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
                else -> {
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    "${localDate.dayOfMonth} ${localDate.month.name.take(3)}"
                }
            }
        } catch (e: Exception) {
            "a while ago"
        }
    }

    fun onToggleWarningsExpansion() {
        _uiState.update { it.copy(isWarningsExpanded = !it.isWarningsExpanded) }
    }

    fun showSyncMileageDialog() {
        val currentMileage = _uiState.value.selectedVehicleInfo?.mileage
        _uiState.update {
            it.copy(
                isSyncMileageDialogVisible = true,
                currentMileageForDialog = currentMileage
            )
        }
    }

    fun dismissSyncMileageDialog() {
        _uiState.update {
            it.copy(
                isSyncMileageDialogVisible = false,
                currentMileageForDialog = null
            )
        }
    }
}