package com.example.cartrack.feature.home.presentation.notifications.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Divider
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
import com.example.cartrack.feature.navigation.Routes

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
                        // Fără padding sau arrangement aici, vom controla totul în `items`
                    ) {
                        uiState.groupedNotifications.forEach { (timeCategory, notificationsInCategory) ->
                            if (notificationsInCategory.isNotEmpty()) {
                                stickyHeader {
                                    TimeCategoryHeader(
                                        title = timeCategory.displayName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }

                                items(
                                    notificationsInCategory,
                                    key = { notification -> "notification_${notification.id}" }
                                ) { notification ->
                                    NotificationListItem(
                                        notification = notification,
                                        onClick = {
                                            if (notification.reminderId > 0) {
                                                navController.navigate(Routes.reminderDetailRoute(notification.reminderId))
                                            }
                                        }
                                    )

                                    // Adaugă un Divider după fiecare item, cu indentare
                                    if (notification != notificationsInCategory.last()) {
                                        Divider(
                                            modifier = Modifier.padding(start = 76.dp), // 48dp (icon) + 16dp (spacer) + 12dp (padding intern) = ~76dp
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
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
    )
}