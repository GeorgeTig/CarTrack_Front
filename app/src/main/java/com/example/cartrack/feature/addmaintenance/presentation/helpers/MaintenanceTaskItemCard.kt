package com.example.cartrack.feature.addmaintenance.presentation.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.addmaintenance.presentation.CUSTOM_TASK_NAME_OPTION
import com.example.cartrack.feature.addmaintenance.presentation.MaintenanceTask
import com.example.cartrack.feature.addmaintenance.presentation.MaintenanceType
import com.example.cartrack.feature.addmaintenance.presentation.UiMaintenanceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTaskItemCard(
    item: UiMaintenanceItem,
    index: Int, // Pentru afișarea numărului task-ului
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
        availableMaintenanceTypes.find { it.id == item.selectedMaintenanceTypeId }?.name ?: "Select Type"
    }

    val tasksForCurrentType = remember(item.selectedMaintenanceTypeId) {
        // Apelăm funcția pasată pentru a obține sarcinile filtrate + opțiunea Custom
        getTasksForSelectedType(item.selectedMaintenanceTypeId)
    }

    val selectedTaskNameDisplay = remember(item.showCustomTaskNameInput, item.selectedTaskName) {
        // Dacă modul custom e activ pentru input, dropdown-ul de task arată "Custom Task..."
        // Altfel, arată numele task-ului selectat sau "Select Task"
        if (item.showCustomTaskNameInput) {
            CUSTOM_TASK_NAME_OPTION
        } else {
            item.selectedTaskName ?: "Select Task"
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
                Text(
                    "Task #${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemoveItem, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, "Remove Task", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Dropdown pentru TIPUL principal de mentenanță
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedMainTypeName, // Acum este String non-null
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Maintenance Type*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = itemError != null && item.selectedMaintenanceTypeId == null
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    availableMaintenanceTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onTypeSelected(type)
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Dropdown pentru SARCINA de mentenanță
            if (item.selectedMaintenanceTypeId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = taskDropdownExpanded,
                    onExpandedChange = { taskDropdownExpanded = !taskDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTaskNameDisplay, // Acum este String non-null
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Specific Task / Service*") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = itemError != null && item.selectedTaskName.isNullOrBlank() && !item.showCustomTaskNameInput
                    )
                    ExposedDropdownMenu(
                        expanded = taskDropdownExpanded,
                        onDismissRequest = { taskDropdownExpanded = false }
                    ) {
                        tasksForCurrentType.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.name) },
                                onClick = {
                                    onTaskSelected(task)
                                    taskDropdownExpanded = false
                                    if (task.isCustomOption) {
                                        // Încearcă să ceri focus după ce starea UI s-a actualizat
                                        // și câmpul text a devenit vizibil.
                                        // Acest LaunchedEffect rulează când `item.showCustomTaskNameInput` devine true.
                                        // (Necesită ca acest Composable să fie inteligent la recompuneri)
                                        // O soluție mai simplă poate fi necesară dacă focusul e problematic.
                                        if (item.showCustomTaskNameInput) { // Verifică din nou starea curentă
                                            customNameFocusRequester.requestFocus()
                                        }
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Câmp text pentru numele custom al sarcinii
            if (item.showCustomTaskNameInput) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = item.customTaskNameInput,
                    onValueChange = onCustomTaskNameChanged,
                    label = { Text("Describe Custom Task*") },
                    placeholder = { Text("e.g., Replaced front left indicator bulb") },
                    modifier = Modifier.fillMaxWidth().focusRequester(customNameFocusRequester),
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = itemError != null && item.customTaskNameInput.isBlank(),
                    supportingText = {
                        if (itemError != null && item.customTaskNameInput.isBlank()) {
                            Text(itemError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Afișează eroarea generală pentru item (dacă există și nu e deja afișată la câmpul custom)
            if (itemError != null && !(item.showCustomTaskNameInput && item.customTaskNameInput.isBlank() && itemError.contains("Custom"))) {
                Text(itemError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}