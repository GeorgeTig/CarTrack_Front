package com.example.cartrack.features.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.domain.repository.VehicleRepository
import com.example.cartrack.core.services.jwt.JwtDecoder
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleManager: VehicleManager,
    // Injectăm și aceste dependențe pentru a obține ID-ul clientului și pentru a apela Quick Sync
    private val tokenManager: TokenManager,
    private val jwtDecoder: JwtDecoder,
    @ApplicationContext private val context: Context // Opțional, pentru viitor (ex: Geocoder)
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var fetchDetailsJob: Job? = null
    private val logTag = "HomeViewModel"

    init {
        viewModelScope.launch {
            // Reacționează la schimbarea vehiculului selectat
            _uiState.map { it.selectedVehicle?.id }.distinctUntilChanged().collectLatest { vehicleId ->
                fetchDetailsJob?.cancel()
                if (vehicleId != null) {
                    fetchSelectedVehicleDetails(vehicleId)
                }
            }
        }
    }

    fun loadVehicles(forceRefresh: Boolean = false) {
        if (_uiState.value.vehicles.isNotEmpty() && !forceRefresh) {
            if (_uiState.value.isLoading) _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            vehicleRepository.getVehiclesByClientId().onSuccess { fetchedVehicles ->
                if (fetchedVehicles.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, vehicles = emptyList(), selectedVehicle = null) }
                } else {
                    val lastUsedId = vehicleManager.lastVehicleIdFlow.firstOrNull()
                    val vehicleToSelect = fetchedVehicles.find { it.id == lastUsedId } ?: fetchedVehicles.first()

                    _uiState.update { it.copy(isLoading = false, vehicles = fetchedVehicles, selectedVehicle = vehicleToSelect) }

                    // Salvăm selecția doar dacă s-a schimbat
                    if (_uiState.value.selectedVehicle?.id != lastUsedId) {
                        vehicleManager.saveLastVehicleId(vehicleToSelect.id)
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun fetchSelectedVehicleDetails(vehicleId: Int) {
        _uiState.update { it.copy(isLoadingDetails = true, warnings = emptyList(), dailyUsage = emptyList()) }
        fetchDetailsJob = viewModelScope.launch {
            // Apeluri paralele pentru eficiență
            val infoDeferred = async { vehicleRepository.getVehicleInfo(vehicleId) }
            val remindersDeferred = async { vehicleRepository.getRemindersByVehicleId(vehicleId) }
            val usageDeferred = async {
                val timeZoneId = TimeZone.currentSystemDefault().id
                vehicleRepository.getDailyUsage(vehicleId, timeZoneId)
            }

            val infoResult = infoDeferred.await()
            val remindersResult = remindersDeferred.await()
            val usageResult = usageDeferred.await()

            infoResult.onSuccess { info ->
                _uiState.update { it.copy(selectedVehicleInfo = info, lastSyncTime = formatLastSyncTime(info.lastUpdate)) }
            }.onFailure { e -> Log.e(logTag, "Failed to fetch info for vehicle $vehicleId: ${e.message}") }

            remindersResult.onSuccess { reminders ->
                val warnings = reminders.filter { it.isActive && it.statusId in setOf(2, 3) }.sortedByDescending { it.statusId }
                _uiState.update { it.copy(warnings = warnings) }
            }.onFailure { e -> Log.e(logTag, "Failed to fetch reminders for vehicle $vehicleId: ${e.message}") }

            usageResult.onSuccess { usage ->
                _uiState.update { it.copy(dailyUsage = usage) }
            }.onFailure { e -> Log.e(logTag, "Failed to fetch daily usage: ${e.message}") }

            _uiState.update { it.copy(isLoadingDetails = false) }
        }
    }

    fun onVehicleSelected(vehicleId: Int) {
        if (vehicleId != _uiState.value.selectedVehicle?.id) {
            _uiState.value.vehicles.find { it.id == vehicleId }?.let { newSelectedVehicle ->
                viewModelScope.launch { vehicleManager.saveLastVehicleId(vehicleId) }
                _uiState.update { it.copy(selectedVehicle = newSelectedVehicle) }
            }
        }
    }

    fun onToggleWarningsExpansion() {
        _uiState.update { it.copy(isWarningsExpanded = !it.isWarningsExpanded) }
    }

    // --- Funcții pentru Quick Sync Dialog ---
    fun showSyncMileageDialog() {
        _uiState.update { it.copy(isSyncMileageDialogVisible = true) }
    }

    fun dismissSyncMileageDialog() {
        _uiState.update { it.copy(isSyncMileageDialogVisible = false) }
    }

    fun syncMileage(mileage: String) {
        dismissSyncMileageDialog()
        val mileageValue = mileage.toDoubleOrNull()
        val vehicleId = _uiState.value.selectedVehicle?.id

        if (mileageValue == null || vehicleId == null) {
            Log.e(logTag, "Invalid mileage or vehicle ID for sync.")
            // TODO: Emite un eveniment de eroare pentru a afișa un Toast
            return
        }

        viewModelScope.launch {
            vehicleRepository.addMileageReading(vehicleId, mileageValue).onSuccess {
                Log.d(logTag, "Mileage synced successfully. Refreshing details...")
                fetchSelectedVehicleDetails(vehicleId)
            }.onFailure { e ->
                Log.e(logTag, "Mileage sync failed: ${e.message}")
                // TODO: Emite un eveniment de eroare
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocationAndWeather() {
        // TODO: Implementează logica pentru permisiuni, FusedLocationProvider, Geocoder și API de vreme
        // La final, actualizează uiState-ul cu noile date:
        // _uiState.update { it.copy(locationData = LocationData(city = "Bucharest", temperature = "25°", iconUrl = "...")) }
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
                duration.inWholeDays < 1 -> "${duration.inWholeHours}h ago" // Mai scurt
                duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
                else -> {
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    "${localDate.dayOfMonth} ${localDate.month.name.take(3)}" // Ex: 15 Jun
                }
            }
        } catch (e: Exception) {
            "a while ago"
        }
    }
}