package com.example.cartrack.feature.addmaintenance.presentation

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    navController: NavHostController,
    viewModel: AddMaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Maintenance log saved!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveStatus()
            navController.popBackStack()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.resetSaveStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.currentVehicleSeries != "Vehicle" && uiState.currentVehicleSeries.isNotBlank())
                            "Log for ${uiState.currentVehicleSeries}"
                        else "Log New Maintenance"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.saveMaintenance() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Save, "Save Maintenance Log")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp)
                ) {
                    item {
                        Text("Service Details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                        MaintenanceGeneralInfoSection(
                            date = uiState.date,
                            dateError = uiState.dateError,
                            onDateChange = viewModel::onDateChange,
                            mileage = uiState.mileage,
                            mileageError = uiState.mileageError,
                            onMileageChange = viewModel::onMileageChange
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Performed Maintenance Tasks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    if (uiState.maintenanceItems.isEmpty()) {
                        item {
                            Text(
                                "Click 'Add Task' to log a maintenance item.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        itemsIndexed(uiState.maintenanceItems, key = { _, item -> item.id }) { index, item ->
                            MaintenanceTaskItemCard(
                                item = item,
                                index = index,
                                availableMaintenanceTypes = uiState.availableMaintenanceTypes,
                                getTasksForSelectedType = { typeId -> viewModel.getTasksForType(typeId) },
                                itemError = uiState.itemErrors[item.id],
                                onTypeSelected = { selectedType ->
                                    viewModel.onMaintenanceTypeSelected(item.id, selectedType)
                                },
                                onTaskSelected = { selectedTask ->
                                    viewModel.onMaintenanceTaskSelected(item.id, selectedTask)
                                },
                                onCustomTaskNameChanged = { customName ->
                                    viewModel.onCustomTaskNameChanged(item.id, customName)
                                },
                                onRemoveItem = { viewModel.removeMaintenanceItem(item.id) }
                            )
                            if (index < uiState.maintenanceItems.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { viewModel.addMaintenanceItem() },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Task")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Task")
                        }
                    }

                    item {
                        Text("Additional Info (Optional)", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                        MaintenanceOptionalInfoSection(
                            serviceProvider = uiState.serviceProvider,
                            onServiceProviderChange = viewModel::onServiceProviderChange,
                            cost = uiState.cost,
                            costError = uiState.costError,
                            onCostChange = viewModel::onCostChange,
                            notes = uiState.notes,
                            onNotesChange = viewModel::onNotesChange
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .clickable(enabled = false, onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun MaintenanceGeneralInfoSection(
    date: String,
    dateError: String?,
    onDateChange: (String) -> Unit,
    mileage: String,
    mileageError: String?,
    onMileageChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MaintenanceDateField(
            selectedDate = date,
            onDateSelected = onDateChange,
            dateError = dateError
        )
        OutlinedTextField(
            value = mileage,
            onValueChange = onMileageChange,
            label = { Text("Current Mileage (km)*") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            isError = mileageError != null,
            supportingText = { mileageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun MaintenanceOptionalInfoSection(
    serviceProvider: String,
    onServiceProviderChange: (String) -> Unit,
    cost: String,
    costError: String?,
    onCostChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = serviceProvider,
            onValueChange = onServiceProviderChange,
            label = { Text("Service Provider") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = cost,
            onValueChange = onCostChange,
            label = { Text("Total Cost") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            isError = costError != null,
            supportingText = { costError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes / Comments") },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun MaintenanceDateField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    dateError: String?
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val (initialYear, initialMonth, initialDay) = remember(selectedDate) {
        try {
            val parsedDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
            Triple(parsedDate.year, parsedDate.monthValue - 1, parsedDate.dayOfMonth)
        } catch (e: Exception) {
            val cal = Calendar.getInstance()
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    val datePickerDialog = remember(context, initialYear, initialMonth, initialDay) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val localDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                focusManager.moveFocus(FocusDirection.Down)
            }, initialYear, initialMonth, initialDay
        ).apply { datePicker.maxDate = System.currentTimeMillis() }
    }

    OutlinedTextField(
        value = selectedDate,
        onValueChange = { },
        label = { Text("Date of Service*") },
        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
        leadingIcon = { Icon(Icons.Filled.CalendarToday, "Select Date") },
        trailingIcon = { Icon(Icons.Filled.ExpandMore, "Open Date Picker") },
        readOnly = true,
        isError = dateError != null,
        supportingText = { dateError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTaskItemCard(
    item: UiMaintenanceItem,
    index: Int,
    availableMaintenanceTypes: List<MaintenanceType>,
    getTasksForSelectedType: (typeId: Int?) -> List<MaintenanceTask>,
    itemError: String?,
    onTypeSelected: (MaintenanceType?) -> Unit,
    onTaskSelected: (MaintenanceTask?) -> Unit,
    onCustomTaskNameChanged: (String) -> Unit,
    onRemoveItem: () -> Unit
) {
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var taskDropdownExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val customNameFocusRequester = remember { FocusRequester() }

    val selectedMainTypeName = remember(item.selectedMaintenanceTypeId, availableMaintenanceTypes) {
        availableMaintenanceTypes.find { it.id == item.selectedMaintenanceTypeId }?.name ?: "Select Category..." // Placeholder pentru tip
    }

    val tasksForCurrentItemType = remember(item.selectedMaintenanceTypeId) {
        getTasksForSelectedType(item.selectedMaintenanceTypeId)
    }

    val selectedTaskNameDisplay = remember(item.selectedTaskName, item.showCustomTaskNameInput) {
        if (item.showCustomTaskNameInput) {
            CUSTOM_TASK_NAME_OPTION
        } else {
            item.selectedTaskName ?: tasksForCurrentItemType.firstOrNull { !it.isCustomOption && it.name != "Select task or go custom..." && it.name != "No specific tasks found..." }?.name ?: "Select Task..."
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Task #${index + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemoveItem, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, "Remove Task", tint = MaterialTheme.colorScheme.error)
                }
            }

            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMainTypeName,
                    onValueChange = {}, readOnly = true, label = { Text("Maintenance Category*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    isError = itemError != null && (item.selectedMaintenanceTypeId == null || item.selectedMaintenanceTypeId == 0)
                )
                ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                    availableMaintenanceTypes.forEach { type -> // Afișează toate, inclusiv placeholder-ul
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = { onTypeSelected(type); typeDropdownExpanded = false }
                        )
                    }
                }
            }

            if (item.selectedMaintenanceTypeId != null && item.selectedMaintenanceTypeId != 0) {
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = taskDropdownExpanded,
                    onExpandedChange = { taskDropdownExpanded = !taskDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTaskNameDisplay,
                        onValueChange = {}, readOnly = true, label = { Text("Specific Service / Task*") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        isError = itemError != null && item.selectedTaskName.isNullOrBlank() && !item.showCustomTaskNameInput ||
                                (itemError != null && item.showCustomTaskNameInput && item.customTaskNameInput.isBlank())
                    )
                    ExposedDropdownMenu(expanded = taskDropdownExpanded, onDismissRequest = { taskDropdownExpanded = false }) {
                        tasksForCurrentItemType.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.name) },
                                onClick = {
                                    onTaskSelected(task)
                                    taskDropdownExpanded = false
                                    if (task.isCustomOption) {
                                        customNameFocusRequester.requestFocus()
                                    } else {
                                        focusManager.clearFocus(true) // Forțează ascunderea tastaturii
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (item.showCustomTaskNameInput) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = item.customTaskNameInput,
                    onValueChange = onCustomTaskNameChanged,
                    label = { Text("Describe Custom Task*") },
                    placeholder = { Text("e.g., Replaced front left indicator bulb") },
                    modifier = Modifier.fillMaxWidth().focusRequester(customNameFocusRequester),
                    singleLine = false, maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = itemError != null && item.customTaskNameInput.isBlank(),
                    supportingText = { if (itemError != null && item.customTaskNameInput.isBlank()) { Text(itemError, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (itemError != null && !(item.showCustomTaskNameInput && item.customTaskNameInput.isBlank() && itemError.contains("Custom"))) {
                Text(itemError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}