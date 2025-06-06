package com.example.cartrack.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(icon, contentDescription = dialogTitle, modifier = Modifier.size(ButtonDefaults.IconSize)) },
        title = {
            Text(
                text = dialogTitle,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(dialogText, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmation() },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() },
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        shape = MaterialTheme.shapes.large
    )
}