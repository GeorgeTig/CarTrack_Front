package com.example.cartrack.features.add_maintenance.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.cartrack.features.add_maintenance.CUSTOM_TASK_NAME_OPTION
import com.example.cartrack.features.add_maintenance.MaintenanceTask
import com.example.cartrack.features.add_maintenance.MaintenanceType
import com.example.cartrack.features.add_maintenance.UiMaintenanceItem

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
    onRemoveItem: () -> Unit,
    isEnabled: Boolean
) {
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var taskDropdownExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val customNameFocusRequester = remember { FocusRequester() }

    val selectedTypeName = availableMaintenanceTypes.find { it.id == item.selectedMaintenanceTypeId }?.name ?: "Select Type"
    val tasksForType = remember(item.selectedMaintenanceTypeId) { getTasksForSelectedType(item.selectedMaintenanceTypeId) }

    val selectedTaskName = when {
        item.showCustomTaskNameInput -> CUSTOM_TASK_NAME_OPTION
        else -> item.selectedTaskName ?: "Select Task"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Task #${index + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (isEnabled) {
                    IconButton(onClick = onRemoveItem) { Icon(Icons.Filled.Delete, "Remove Task", tint = MaterialTheme.colorScheme.error) }
                }
            }

            ExposedDropdownMenuBox(expanded = typeDropdownExpanded, onExpandedChange = { if (isEnabled) typeDropdownExpanded = !typeDropdownExpanded }) {
                OutlinedTextField(
                    value = selectedTypeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Maintenance Type*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = itemError != null && item.selectedMaintenanceTypeId == null,
                    enabled = isEnabled
                )
                ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                    availableMaintenanceTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type.name) }, onClick = { onTypeSelected(type); typeDropdownExpanded = false })
                    }
                }
            }

            AnimatedVisibility(visible = item.selectedMaintenanceTypeId != null) {
                ExposedDropdownMenuBox(expanded = taskDropdownExpanded, onExpandedChange = { if (isEnabled) taskDropdownExpanded = !taskDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedTaskName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Specific Task*") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        isError = itemError != null && item.selectedTaskName == null && !item.showCustomTaskNameInput,
                        enabled = isEnabled
                    )
                    ExposedDropdownMenu(expanded = taskDropdownExpanded, onDismissRequest = { taskDropdownExpanded = false }) {
                        tasksForType.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.name) },
                                onClick = {
                                    onTaskSelected(task)
                                    taskDropdownExpanded = false
                                    if (task.isCustomOption) {
                                        customNameFocusRequester.requestFocus()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = item.showCustomTaskNameInput) {
                OutlinedTextField(
                    value = item.customTaskNameInput,
                    onValueChange = onCustomTaskNameChanged,
                    label = { Text("Describe Custom Task*") },
                    modifier = Modifier.fillMaxWidth().focusRequester(customNameFocusRequester),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = itemError != null && item.customTaskNameInput.isBlank(),
                    supportingText = { if (itemError != null && item.customTaskNameInput.isBlank()) Text("Description cannot be empty") },
                    enabled = isEnabled
                )
            }

            if (itemError != null) {
                Text(itemError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
            }
        }
    }
}