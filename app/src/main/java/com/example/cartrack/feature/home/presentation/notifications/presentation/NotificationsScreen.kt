package com.example.cartrack.feature.home.presentation.notifications.presentation

import androidx.compose.foundation.ExperimentalFoundationApi // Necesar pentru stickyHeader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Sau background, depinde de tema
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
                // Poți adăuga o umbră subtilă sub TopAppBar dacă dorești
                // modifier = Modifier.shadow(elevation = 2.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Fundalul general al ecranului
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplică padding-ul de la Scaffold
        ) {
            when {
                // Stare de încărcare inițială
                uiState.isLoading && uiState.groupedNotifications.isEmpty() && uiState.error == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Stare de eroare
                uiState.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshNotifications() }) {
                            Text("Retry")
                        }
                    }
                }
                // Nu sunt notificări (după încărcare, fără erori)
                !uiState.isLoading && uiState.groupedNotifications.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Text("You have no notifications yet.", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The notification indicator has been cleared.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Afișează lista de notificări grupate
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp), // Padding pentru listă
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Spațiu între item-uri/headere
                    ) {
                        // Iterează prin categoriile de timp din uiState.groupedNotifications
                        // Map-ul este deja sortat în ViewModel (dacă folosești LinkedHashMap)
                        uiState.groupedNotifications.forEach { (timeCategory, notificationsInCategory) ->
                            // Verifică dacă există notificări în categorie înainte de a afișa header-ul
                            if (notificationsInCategory.isNotEmpty()) {
                                stickyHeader { // Header persistent pentru fiecare categorie
                                    TimeCategoryHeader(title = timeCategory.displayName)
                                }
                                items(
                                    notificationsInCategory,
                                    key = { notification -> "notification_${notification.id}" }
                                ) { notification ->
                                    NotificationListItem(
                                        notification = notification,
                                        // Adaugă un mic padding sub fiecare item, cu excepția ultimului din categorie
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                // Adaugă un spațiu mai mare sau un separator între categorii, dacă dorești
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable pentru header-ul categoriei de timp
@Composable
fun TimeCategoryHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)) // Fundal ușor transparent pentru sticky
            .padding(vertical = 8.dp, horizontal = 4.dp) // Padding intern pentru textul header-ului
    )
}