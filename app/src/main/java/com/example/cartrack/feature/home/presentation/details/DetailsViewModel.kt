package com.example.cartrack.feature.home.presentation.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.vehicle.data.model.VehicleBodyResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleEngineResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleModelResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleUsageStatsResponseDto
import com.example.cartrack.core.vehicle.domain.repository.VehicleRepository // Needs detail methods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val vehicleCacheManager: VehicleManager // Inject cache manager to observe ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var currentDataLoadingJob: Job? = null
    private val logTag = "DetailsViewModel"

    init {
        observeSelectedVehicle()
    }

    private fun observeSelectedVehicle() {
        viewModelScope.launch {
            vehicleCacheManager.lastVehicleIdFlow
                // .filterNotNull() // Decide if you want to react to null ID (e.g., clear UI)
                .distinctUntilChanged()
                .collect { vehicleId ->
                    Log.d(logTag, "Observed selected vehicle ID change from CacheManager: $vehicleId")
                    // Cancel any ongoing detail loading for the previous ID
                    currentDataLoadingJob?.cancel()
                    // Update state for the new ID, reset details and visibility
                    _uiState.update {
                        // Create a fresh state for the new ID
                        DetailsUiState(
                            selectedVehicleId = vehicleId,
                            visibleDetail = VisibleDetailSection.NONE, // Hide details
                            isLoadingDetails = false
                            // Detail DTOs are null by default
                        )
                    }
                    // Note: No automatic fetching here, wait for user click
                }
        }
    }

    fun showDetailSection(section: VisibleDetailSection) {
        val vehicleId = _uiState.value.selectedVehicleId
        if (vehicleId == null) {
            Log.w(logTag, "showDetailSection called but selectedVehicleId is null.")
            // Optionally set an error, or just do nothing
            // _uiState.update { it.copy(error = "No vehicle selected.") }
            return
        }

        currentDataLoadingJob?.cancel()

        // Hide if clicking the same section again
        if (_uiState.value.visibleDetail == section) {
            _uiState.update { it.copy(visibleDetail = VisibleDetailSection.NONE, isLoadingDetails = false) }
            return
        }

        // Set loading state and the section to be shown
        _uiState.update { currentState ->
            currentState.copy(
                isLoadingDetails = true,
                visibleDetail = section,
                error = null,
                // Clear data for other sections
                engineDetails = if (section == VisibleDetailSection.ENGINE) currentState.engineDetails else null,
                modelDetails = if (section == VisibleDetailSection.MODEL) currentState.modelDetails else null,
                bodyDetails = if (section == VisibleDetailSection.BODY) currentState.bodyDetails else null,
            )
        }

        // Launch job to fetch the specific detail data
        currentDataLoadingJob = viewModelScope.launch {
            Log.d(logTag, "Loading detail section $section for vehicle ID $vehicleId")
            val result = when (section) {
                VisibleDetailSection.ENGINE -> vehicleRepository.getVehicleEngine(vehicleId)
                VisibleDetailSection.MODEL -> vehicleRepository.getVehicleModel(vehicleId)
                VisibleDetailSection.BODY -> vehicleRepository.getVehicleBody(vehicleId)
                VisibleDetailSection.NONE -> null
            }

            // Before updating UI, check if the selected vehicle ID or target section hasn't changed
            if (_uiState.value.selectedVehicleId != vehicleId || _uiState.value.visibleDetail != section) {
                Log.w(logTag, "Vehicle ID or Visible Section changed during fetch for $section. Aborting update.")
                // Ensure loading indicator is turned off if it was for this request
                if (_uiState.value.visibleDetail == section) _uiState.update { it.copy(isLoadingDetails = false) }
                return@launch
            }

            // Update UI with fetched data or error
            if (result != null) {
                result.onSuccess { data ->
                    _uiState.update { state ->
                        when (section) {
                            VisibleDetailSection.ENGINE -> state.copy(engineDetails = data as? VehicleEngineResponseDto, isLoadingDetails = false)
                            VisibleDetailSection.MODEL -> state.copy(modelDetails = data as? VehicleModelResponseDto, isLoadingDetails = false)
                            VisibleDetailSection.BODY -> state.copy(bodyDetails = data as? VehicleBodyResponseDto, isLoadingDetails = false)
                            VisibleDetailSection.NONE -> state
                        }.copy(error = null) // Clear error on success
                    }
                    Log.d(logTag, "Success loading section $section for ID $vehicleId")
                }
                result.onFailure { e ->
                    Log.e(logTag, "Error loading section $section for ID $vehicleId: ${e.message}", e)
                    _uiState.update { it.copy(error = e.message ?: "Failed.", isLoadingDetails = false) }
                }
            } else {
                _uiState.update { it.copy(isLoadingDetails = false) } // Should not happen if section != NONE
            }
        }
    }
}