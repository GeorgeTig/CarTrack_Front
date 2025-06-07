package com.example.cartrack.features.add_vehicle

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto

enum class AddVehicleStep { VIN, SERIES_YEAR, ENGINE_DETAILS, BODY_DETAILS, VEHICLE_INFO, CONFIRM }

data class AddVehicleUiState(
    val currentStep: AddVehicleStep = AddVehicleStep.VIN,
    val totalSteps: Int = AddVehicleStep.entries.size,
    val vinInput: String = "",
    val vinValidationError: String? = null,
    val isLoadingVinDetails: Boolean = false,
    val allDecodedOptions: List<VinDecodedResponseDto> = emptyList(),
    val availableSeriesAndYears: List<Pair<String, Int>> = emptyList(),
    val selectedSeriesName: String? = null,
    val selectedYear: Int? = null,
    val availableEngineSizes: List<Double> = emptyList(),
    val selectedEngineSize: Double? = null,
    val availableEngineTypes: List<String> = emptyList(),
    val selectedEngineType: String? = null,
    val availableTransmissions: List<String> = emptyList(),
    val selectedTransmission: String? = null,
    val availableDriveTypes: List<String> = emptyList(),
    val selectedDriveType: String? = null,
    val confirmedEngineId: Int? = null,
    val availableBodyTypes: List<String> = emptyList(),
    val selectedBodyType: String? = null,
    val availableDoorNumbers: List<Int> = emptyList(),
    val selectedDoorNumber: Int? = null,
    val availableSeatNumbers: List<Int> = emptyList(),
    val selectedSeatNumber: Int? = null,
    val confirmedBodyId: Int? = null,
    val mileageInput: String = "",
    val mileageValidationError: String? = null,
    val determinedModelId: Int? = null,
    val isLoadingNextStep: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaveSuccess: Boolean = false,
    val isNextEnabled: Boolean = false,
    val isPreviousEnabled: Boolean = false,
    val hasAttemptedNext: Boolean = false
)

fun AddVehicleUiState.getFinalConfirmedModelDetailsForDisplay(): String {
    val producer = allDecodedOptions.find { it.seriesName == selectedSeriesName && it.vehicleModelInfo.any { vm -> vm.year == selectedYear } }?.producer
    return buildString {
        producer?.let { append("$it ") }
        selectedSeriesName?.let { append("$it ") }
        selectedYear?.let { append("($it) ") }
    }.trim().replace("  ", " ")
}