package com.example.cartrack.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.ConfirmationDialog
import com.example.cartrack.features.auth.AuthViewModel
import com.example.cartrack.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        ConfirmationDialog(
            onDismissRequest = { showLogoutDialog = false },
            onConfirmation = {
                showLogoutDialog = false
                authViewModel.logout() // Acțiunea de logout este delegată ViewModel-ului
            },
            dialogTitle = "Log Out?",
            dialogText = "Are you sure you want to log out from your account?",
            icon = Icons.AutoMirrored.Filled.Logout
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { SettingsCategory("Account") }
            item {
                SettingsItem(
                    title = "Edit Profile",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate(Routes.EDIT_PROFILE) }
                )
            }
            item {
                SettingsItem(
                    title = "Change Password",
                    icon = Icons.Default.Password,
                    onClick = { /* TODO: Navigate to ChangePasswordScreen */ }
                )
            }

            item { Divider(Modifier.padding(vertical = 16.dp)) }

            item { SettingsCategory("Support") }
            item {
                SettingsItem(
                    title = "Help & FAQ",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { /* TODO: Open URL */ }
                )
            }

            item { Divider(Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) }

            item {
                SettingsItem(
                    title = "Log Out",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    contentColor: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = contentColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
    }
}