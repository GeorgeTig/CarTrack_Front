package com.example.cartrack.feature.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// Import ViewModel when created

@Composable
fun ProfileScreen(
    // viewModel: ProfileViewModel = hiltViewModel(), // Inject later
    onLogout: () -> Unit // Action for logging out
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally){
            Text("Profile Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Text("(User details and settings here)")
            Spacer(modifier = Modifier.height(20.dp))

            // Proper place for Logout Button
            Button(onClick = onLogout) {
                Text("Log Out")
            }
        }
    }
}