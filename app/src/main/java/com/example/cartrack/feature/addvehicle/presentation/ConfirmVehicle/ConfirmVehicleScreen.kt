package com.example.cartrack.feature.addvehicle.presentation

// ... Keep imports ...
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cartrack.feature.addvehicle.data.model.*
import com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle.ConfirmVehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmVehicleScreen(
    viewModel: ConfirmVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVehicleAddedSuccessfully: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ... (Error handling LaunchedEffect remains the same) ...
    LaunchedEffect(uiState.error) { /* ... */ }
    // ... (TODO: Save success LaunchedEffect) ...

    val isAmbiguous = uiState.needsTopLevelSelection || uiState.needsYearSelection || uiState.needsEngineSelection || uiState.needsBodySelection || uiState.needsModelSelection

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { /* ... TopAppBar as before ... */ },
        bottomBar = { /* ... BottomAppBar as before ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ... (Top Level Info display and Ambiguity Warning remain the same) ...

            // --- Selection Sections - With Corrected Lambdas ---

            // Step 1: Top Level Selection (If needed)
            AnimatedVisibility(visible = uiState.availableTopLevelOptions.isNotEmpty()) {
                Column {
                    SelectionSection(
                        title = "Manufacturer / Series",
                        options = uiState.availableTopLevelOptions,
                        selectedOption = uiState.selectedTopLevelIndex?.let { uiState.availableTopLevelOptions.getOrNull(it) },
                        // *** FIX: Lambda extracts index and calls VM ***
                        onOptionSelected = { selectedDto ->
                            val index = uiState.availableTopLevelOptions.indexOf(selectedDto)
                            if (index != -1) {
                                viewModel.selectTopLevelOption(index)
                            }
                        },
                        needsSelection = uiState.needsTopLevelSelection,
                        displayContent = { topLevel -> Text("${topLevel.producer ?: "Unknown"} - ${topLevel.seriesName ?: "Unknown"}") }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Step 2: Year Selection (Visible if Top Level selected)
            AnimatedVisibility(visible = uiState.selectedTopLevelIndex != null && uiState.availableYears.isNotEmpty(), modifier = Modifier.animateContentSize()) {
                Column {
                    SelectionSection(
                        title = "Year",
                        options = uiState.availableYears,
                        selectedOption = uiState.selectedYear,
                        onOptionSelected = viewModel::selectYear, // No change needed - already passes Int
                        needsSelection = uiState.needsYearSelection,
                        displayContent = { year -> Text(year.toString()) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Step 3: Engine Selection (Visible if Year selected)
            AnimatedVisibility(visible = uiState.selectedYear != null && uiState.availableEngines.isNotEmpty(), modifier = Modifier.animateContentSize()) {
                Column {
                    SelectionSection(
                        title = "Engine",
                        options = uiState.availableEngines,
                        selectedOption = uiState.availableEngines.find { it.engineId == uiState.selectedEngineId },
                        // *** FIX: Lambda extracts ID and calls VM ***
                        onOptionSelected = { engine -> viewModel.selectEngine(engine.engineId) },
                        needsSelection = uiState.needsEngineSelection,
                        displayContent = { engine ->
                            Text(buildString { /* ... engine details ... */
                                append(engine.engineType ?: "Engine")
                                engine.size?.let { append(" ${it}L") }
                                engine.horsepower?.let { append(" ${it}hp") }
                                engine.transmission?.let { append(" $it") }
                                engine.driveType?.let { append(" ($it)") }
                            }, style = MaterialTheme.typography.bodyMedium)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Step 4: Body Selection (Visible if Engine selected)
            AnimatedVisibility(visible = uiState.selectedEngineId != null && uiState.availableBodies.isNotEmpty(), modifier = Modifier.animateContentSize()) {
                Column {
                    SelectionSection(
                        title = "Body Style",
                        options = uiState.availableBodies,
                        selectedOption = uiState.availableBodies.find { it.bodyId == uiState.selectedBodyId },
                        // *** FIX: Lambda extracts ID and calls VM ***
                        onOptionSelected = { body -> viewModel.selectBody(body.bodyId) },
                        needsSelection = uiState.needsBodySelection,
                        displayContent = { body ->
                            Text(buildString { /* ... body details ... */
                                append(body.bodyType ?: "Body")
                                body.doorNumber?.let { append(" ${it}-Door") }
                                body.seatNumber?.let { append(" ${it}-Seat") }
                            }, style = MaterialTheme.typography.bodyMedium)
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Step 5: Model Selection (ONLY visible if Body selected AND model choice is still ambiguous)
            AnimatedVisibility(visible = uiState.selectedBodyId != null && uiState.needsModelSelection && uiState.availableModels.isNotEmpty(), modifier = Modifier.animateContentSize()) {
                Column {
                    SelectionSection(
                        title = "Confirm Model",
                        options = uiState.availableModels,
                        selectedOption = uiState.availableModels.find { it.modelId == uiState.selectedModelId },
                        // *** FIX: Lambda extracts ID and calls VM ***
                        onOptionSelected = { model -> viewModel.selectModel(model.modelId) },
                        needsSelection = uiState.needsModelSelection, // True if this section is visible
                        displayContent = { model ->
                            val topLevelData = uiState.selectedTopLevelIndex?.let { index -> uiState.availableTopLevelOptions.getOrNull(index) }
                            Text(buildString { /* ... model details ... */
                                append(topLevelData?.producer ?: "")
                                append(" ")
                                append(topLevelData?.seriesName ?: "Model")
                                append(" (${model.year ?: "N/A"})")
                            })
                        }
                    )
                    // No spacer needed after last section
                }
            }

        } // End Column
    } // End Scaffold
}


/**
 * Reusable Composable for displaying a section with selectable options.
 * Uses RadioButtons for selection when multiple options are available.
 * --- NO CHANGES NEEDED IN THIS COMPOSABLE ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionSection(
    title: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit, // Receives the actual selected option value/object
    needsSelection: Boolean,
    displayContent: @Composable (option: T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            options.isEmpty() -> { /* No options card */
                OutlinedCard(/*...*/) { /*...*/ }
            }
            options.size == 1 && !needsSelection -> { /* Single option card */
                val singleOption = options.first()
                OutlinedCard(/*...*/) { /*...*/ }
            }
            else -> { /* Multiple options card with RadioButtons */
                OutlinedCard(/*...*/) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    // *** Click calls lambda with the full DTO (option) ***
                                    .clickable { onOptionSelected(option) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (option == selectedOption),
                                    // *** Click calls lambda with the full DTO (option) ***
                                    onClick = { onOptionSelected(option) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    displayContent(option) // Display option content
                                }
                            }
                            if (index < options.size - 1) {
                                HorizontalDivider(/*...*/)
                            }
                        }
                    }
                }
            }
        } // End When
    } // End Column
}