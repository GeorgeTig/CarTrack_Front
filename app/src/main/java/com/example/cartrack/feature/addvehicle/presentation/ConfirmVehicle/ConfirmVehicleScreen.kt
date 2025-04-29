    @file:OptIn(ExperimentalMaterial3Api::class) // Enable opt-in for the whole file

    package com.example.cartrack.feature.addvehicle.presentation.ConfirmVehicle

    import androidx.compose.animation.AnimatedVisibility
    import androidx.compose.animation.animateContentSize
    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material.icons.filled.Check
    import androidx.compose.material.icons.filled.Edit
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.lifecycle.compose.collectAsStateWithLifecycle

    @Composable
    fun ConfirmVehicleScreen(
        viewModel: ConfirmVehicleViewModel = hiltViewModel(),
        onNavigateBack: () -> Unit,
        onVehicleAddedSuccessfully: () -> Unit
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        // Show error messages in Snackbar
        LaunchedEffect(uiState.error) {
            uiState.error?.let { errorMsg ->
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Short
                )
                viewModel.errorShown()
            }
        }

        // Trigger navigation on successful save
        LaunchedEffect(uiState.isSaveSuccess) {
            if (uiState.isSaveSuccess) {
                onVehicleAddedSuccessfully()
                viewModel.resetSaveSuccess()
            }
        }

        // UI Layout
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Confirm Vehicle Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    // Add Button
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.confirmAndSaveVehicle() },
                        enabled = uiState.isSelectionComplete && !uiState.isLoading,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (uiState.isLoading && !uiState.isSaveSuccess) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Confirm and Save")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Section: Make (Producer)
                SelectionSectionWrapper(title = "Make") {
                    DropdownSelection(
                        label = "Select Make",
                        options = uiState.availableProducers,
                        selectedOption = uiState.selectedProducer,
                        onOptionSelected = { viewModel.selectProducer(it) },
                        optionToString = { it },
                        isEnabled = !uiState.isLoading,
                        showRequiredMarker = uiState.needsProducerSelection
                    )
                }

                // Section: Series (visible only after a Producer is selected)
                AnimatedVisibility(visible = uiState.selectedProducer != null) {
                    SelectionSectionWrapper(title = "Series") {
                        DropdownSelection(
                            label = "Select Series",
                            options = uiState.availableSeries,
                            selectedOption = uiState.selectedSeriesDto,
                            onOptionSelected = { viewModel.selectSeries(it) },
                            optionToString = { it.seriesName ?: "Unknown Series" },
                            isEnabled = !uiState.isLoading && uiState.availableSeries.isNotEmpty(), // Can be disabled if no series available
                            showRequiredMarker = uiState.needsSeriesSelection
                        )
                    }
                }


                // Section: Year (visible only after a Series is selected)
                AnimatedVisibility(visible = uiState.selectedSeriesDto != null) {
                    SelectionSectionWrapper(title = "Year") {
                        DropdownSelection(
                            label = "Select Year",
                            options = uiState.availableYears,
                            selectedOption = uiState.selectedYear,
                            onOptionSelected = { viewModel.selectYear(it) },
                            optionToString = { it.toString() },
                            isEnabled = !uiState.isLoading,
                            showRequiredMarker = uiState.needsYearSelection
                        )
                    }
                }

                // Section: Engine Details (visible only after Year is selected)
                AnimatedVisibility(visible = uiState.selectedYear != null && uiState.availableEngines.isNotEmpty()) {
                    SelectionSectionWrapper(title = "Engine Details") {
                        // Show Dropdown if confirmation/editing is needed
                        if (uiState.showEngineDropdown) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) { // Allow dropdown to take available space
                                    DropdownSelection(
                                        label = "Select Engine",
                                        options = uiState.availableEngines,
                                        selectedOption = uiState.temporarilySelectedEngine, // Bind to temporary selection
                                        onOptionSelected = { viewModel.selectTemporaryEngine(it) },
                                        optionToString = { it.displayString() }, // Use helper
                                        isEnabled = !uiState.isLoading,
                                        // Show marker if dropdown shown & no temp selection made
                                        showRequiredMarker = uiState.needsEngineConfirmation && uiState.temporarilySelectedEngine == null
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Confirm button enabled only when a temporary selection is made
                                Button(
                                    onClick = { viewModel.confirmEngineSelection() },
                                    enabled = uiState.temporarilySelectedEngine != null && !uiState.isLoading,
                                    contentPadding = PaddingValues(horizontal = 12.dp) // Compact button
                                ) { Text("Confirm") }
                            }
                        }
                        // Show Confirmed Info if engine is confirmed and not currently being edited
                        else if (uiState.confirmedEngine != null && !uiState.isEditingEngine) {
                            ConfirmedInfoDisplay(
                                text = uiState.confirmedEngine.displayString(), // Use helper
                                onEditClick = { viewModel.editEngineSelection() },
                                isEnabled = !uiState.isLoading
                            )
                        }
                        // Optional: Handle case where availableEngines is not empty, but neither dropdown nor confirmed view is shown (shouldn't happen)
                    }
                }

                // Section: Body Style (visible only after Engine is selected)
                AnimatedVisibility(visible = uiState.confirmedEngine != null && uiState.availableBodies.isNotEmpty()) {
                    SelectionSectionWrapper(title = "Body Style") {
                        // Show Dropdown if confirmation/editing is needed
                        if (uiState.showBodyDropdown) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DropdownSelection(
                                        label = "Select Body",
                                        options = uiState.availableBodies,
                                        selectedOption = uiState.temporarilySelectedBody, // Bind to temporary selection
                                        onOptionSelected = { viewModel.selectTemporaryBody(it) },
                                        optionToString = { it.displayString() }, // Use helper
                                        isEnabled = !uiState.isLoading,
                                        // Show marker if dropdown shown & no temp selection made
                                        showRequiredMarker = uiState.needsBodyConfirmation && uiState.temporarilySelectedBody == null
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                // Confirm button enabled only when a temporary selection is made
                                Button(
                                    onClick = { viewModel.confirmBodySelection() },
                                    enabled = uiState.temporarilySelectedBody != null && !uiState.isLoading,
                                    contentPadding = PaddingValues(horizontal = 12.dp) // Compact button
                                ) { Text("Confirm") }
                            }
                        }
                        // Show Confirmed Info if body is confirmed and not currently being edited
                        else if (uiState.confirmedBody != null && !uiState.isEditingBody) {
                            ConfirmedInfoDisplay(
                                text = uiState.confirmedBody.displayString(), // Use helper
                                onEditClick = { viewModel.editBodySelection() },
                                isEnabled = !uiState.isLoading
                            )
                        }
                    }
                }

                // Section: Final Model Confirmation (if needed)
                // Visible only after Body is confirmed AND explicit model selection is required
                AnimatedVisibility(visible = uiState.confirmedBody != null && uiState.needsModelSelection && uiState.availableModels.isNotEmpty()) {
                    SelectionSectionWrapper(title = "Confirm Specific Model") {
                        DropdownSelection(
                            label = "Select Final Model",
                            options = uiState.availableModels,
                            // Find selected DTO based on stored ID
                            selectedOption = uiState.selectedModelId?.let { id -> uiState.availableModels.find { it.modelId == id } },
                            onOptionSelected = { viewModel.selectModel(it) },
                            // Create a descriptive string for the model *without* the ID
                            optionToString = { model ->
                                val engineSpecs = uiState.confirmedEngine?.let { "${it.size?.toString() ?: ""}L ${it.horsepower?.toString() ?: ""}hp" }?.trim() ?: ""
                                val bodySpecs = uiState.confirmedBody?.bodyType ?: ""
                                // Combine known info: Producer Series (Year) EngineSpecs BodySpecs
                                "${uiState.selectedProducer ?: ""} ${uiState.selectedSeriesDto?.seriesName ?: "Model"} (${model.year ?: "N/A"}) $engineSpecs $bodySpecs".trim().replace("  ", " ") // Clean up whitespace
                            },
                            isEnabled = !uiState.isLoading,
                            showRequiredMarker = uiState.selectedModelId == null // Required if this section is visible
                        )
                    }
                }

                // --- User Guidance ---

                // Ambiguity and guidance message
                val needsManualSelection = uiState.needsProducerSelection || uiState.needsSeriesSelection || uiState.needsYearSelection || uiState.needsEngineConfirmation || uiState.needsBodyConfirmation || uiState.needsModelSelection
                AnimatedVisibility(visible = needsManualSelection && !uiState.isSelectionComplete) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)), // Subtle background
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Please complete the highlighted selections above to confirm your vehicle.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
        }
    }

    // Reusable Composable for wrapping selection sections with a title

    /** Generic wrapper for a selection section with title */
    @Composable
    private fun SelectionSectionWrapper(
        title: String,
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Column(modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }

    /** Reusable Dropdown using ExposedDropdownMenuBox */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun <T> DropdownSelection(
        label: String,
        options: List<T>,
        selectedOption: T?,
        onOptionSelected: (T) -> Unit,
        optionToString: (T) -> String,
        isEnabled: Boolean,
        modifier: Modifier = Modifier,
        showRequiredMarker: Boolean = false
    ) {
        var expanded by remember { mutableStateOf(false) }
        val selectedText = selectedOption?.let { optionToString(it) } ?: ""

        ExposedDropdownMenuBox(
            expanded = expanded && isEnabled,
            onExpandedChange = { if (isEnabled) expanded = !expanded },
            modifier = modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = isEnabled,
                isError = showRequiredMarker && selectedText.isEmpty(),
                singleLine = true
            )

            // Dropdown Menu Content
            ExposedDropdownMenu(
                expanded = expanded && isEnabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true) // Match width
            ) {
                if (options.isNotEmpty()) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionToString(option), style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding, // Recommended padding
                        )
                    }
                } else {

                    DropdownMenuItem(
                        text = { Text("No options available", style = MaterialTheme.typography.bodyLarge) },
                        onClick = { },
                        enabled = false,
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }

    /** Displays confirmed info (text) with an Edit button */
    @Composable
    private fun ConfirmedInfoDisplay(
        text: String,
        onEditClick: () -> Unit,
        isEnabled: Boolean,
        modifier: Modifier = Modifier
    ) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                )
                // Edit Button
                IconButton(onClick = onEditClick, enabled = isEnabled) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Standard enabled/disabled tint
                    )
                }
            }
        }
    }