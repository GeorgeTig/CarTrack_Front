package com.example.cartrack.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cartrack.feature.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // --- Categoria Account ---
            item { SettingsCategory(title = "Account") }
            item {
                SettingsItem(
                    title = "Edit Profile",
                    icon = Icons.Default.Person,
                    onClick = {
                        // Navighează la noul ecran
                        navController.navigate(Routes.EDIT_PROFILE)
                    }
                )
            }

            // --- Categoria Security ---
            item { SettingsCategory(title = "Security") }
            item { SettingsItem(title = "Change Password", icon = Icons.Default.Password) { /* TODO: Navighează la ChangePasswordScreen */ } }

            // --- Categoria App ---
            item { SettingsCategory(title = "Appearance") }
            item { SettingsItem(title = "Theme", icon = Icons.Default.Palette) { /* TODO: Afișează dialog de selecție temă */ } }

            // --- Categoria Support ---
            item { SettingsCategory(title = "Support") }
            item { SettingsItem(title = "Help & FAQ", icon = Icons.AutoMirrored.Filled.HelpOutline) { /* TODO: Deschide URL */ } }

            // --- Acțiunea de Logout ---
            item {
                Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp))
                SettingsItem(
                    title = "Log Out",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = onLogout
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
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp)
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