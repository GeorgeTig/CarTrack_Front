package com.example.cartrack.features.add_maintenance.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.features.add_maintenance.CustomTask

@Composable
fun CustomTasksSection(
    tasks: List<CustomTask>,
    onTaskChanged: (String, String) -> Unit,
    onAddTask: () -> Unit,
    onRemoveTask: (String) -> Unit,
    isEnabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Other Unscheduled Tasks", style = MaterialTheme.typography.titleLarge)

        tasks.forEachIndexed { index, task ->
            OutlinedTextField(
                value = task.name,
                onValueChange = { newName -> onTaskChanged(task.id, newName) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Custom task #${index + 1} (optional)") },
                singleLine = true,
                enabled = isEnabled,
                trailingIcon = {
                    if (tasks.size > 1 || task.name.isNotEmpty()) {
                        IconButton(
                            onClick = { onRemoveTask(task.id) },
                            enabled = isEnabled
                        ) {
                            Icon(Icons.Default.Delete, "Remove Task")
                        }
                    }
                }
            )
        }

        TextButton(
            onClick = onAddTask,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text("Add Another Custom Task")
        }
    }
}