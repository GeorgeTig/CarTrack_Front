package com.example.cartrack.feature.home.presentation.statistics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val vehicleCacheManager: VehicleManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var dataLoadingJob: Job? = null
    private val logTag = "StatisticsViewModel"

    init {
        observeSelectedVehicle()
    }

    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Observed selected vehicle ID change from CacheManager: $vehicleId")
                    // Cancel any previous loading job
                    dataLoadingJob?.cancel()
                    // Update the ID in state and trigger loading for the new ID
                    // Reset previous stats data and error
                    _uiState.update {
                        it.copy(
                            selectedVehicleId = vehicleId,
                            isLoading = vehicleId != null, // Start loading if we have an ID
                            statsData = null,
                            error = null
                        )
                    }
                    if (vehicleId != null) {
                        loadStatisticsForCurrentId() // Load data immediately
                    }
                }
        }
    }

    // Renamed function to load data based on the ID stored in state
    private fun loadStatisticsForCurrentId() {
        val vehicleId = _uiState.value.selectedVehicleId
        if (vehicleId == null) {
            Log.w(logTag, "loadStatisticsForCurrentId called but selectedVehicleId is null.")
            // Ensure loading is off if no ID
            if(_uiState.value.isLoading) _uiState.update { it.copy(isLoading = false) }
            return
        }

        // Ensure loading state is true if not already set by the observer
        if (!_uiState.value.isLoading) _uiState.update { it.copy(isLoading = true, error = null) }

        Log.d(logTag, "Loading statistics for vehicle ID: $vehicleId")
        dataLoadingJob = viewModelScope.launch {
            // --- Replace with actual stats fetching ---
            // val statsResult = statisticsRepository.getStatsForVehicle(vehicleId)
            // statsResult.onSuccess { data ->
            //      if (vehicleId == _uiState.value.selectedVehicleId) { // Check if ID is still relevant
            //          _uiState.update { it.copy(isLoading = false, statsData = data, error = null) }
            //          Log.d(logTag, "Successfully loaded stats for ID $vehicleId")
            //      } else { Log.w(...) }
            // }
            // statsResult.onFailure { e ->
            //      if (vehicleId == _uiState.value.selectedVehicleId) { // Check if ID is still relevant
            //          _uiState.update { it.copy(isLoading = false, error = e.message) }
            //          Log.e(...)
            //      } else { Log.w(...) }
            // }
            // --- End Replace ---

            // Placeholder
            kotlinx.coroutines.delay(1200)
            if (vehicleId == _uiState.value.selectedVehicleId) { // Check relevance before UI update
                _uiState.update { it.copy(isLoading = false, statsData = "Stats loaded for $vehicleId", error = null) }
                Log.d(logTag, "Successfully loaded placeholder stats for ID $vehicleId")
            } else {
                Log.w(logTag, "Vehicle ID changed while loading stats for $vehicleId. Aborting update.")
                if(_uiState.value.isLoading) _uiState.update { it.copy(isLoading = false)}
            }
        }
    }

    // Public function for UI to trigger refresh
    fun refreshStatistics() {
        Log.d(logTag, "Refresh statistics requested.")
        dataLoadingJob?.cancel() // Cancel existing load
        loadStatisticsForCurrentId() // Reload based on the ID currently in state
    }
}
