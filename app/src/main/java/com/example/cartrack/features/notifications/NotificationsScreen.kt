package com.example.cartrack.features.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.cartrack.core.ui.components.EmptyState
import com.example.cartrack.features.notifications.components.NotificationListShimmer
import com.example.cartrack.features.notifications.components.NotificationListItem
import com.example.cartrack.features.notifications.components.TimeCategoryHeader
import com.example.cartrack.navigation.Routes
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshNotifications() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.groupedNotifications.isEmpty() -> NotificationListShimmer()
                    uiState.error != null -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                                        Button(onClick = { viewModel.refreshNotifications() }) { Text("Retry") }
                                    }
                                }
                            }
                        }
                    }
                    uiState.groupedNotifications.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Outlined.NotificationsOff,
                            title = "All Caught Up!",
                            subtitle = "You have no new notifications. We'll let you know when something needs your attention."
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            uiState.groupedNotifications.forEach { (timeCategory, notifications) ->
                                stickyHeader {
                                    TimeCategoryHeader(title = timeCategory.displayName)
                                }
                                items(notifications, key = { it.id }) { notification ->
                                    NotificationListItem(
                                        notification = notification,
                                        onClick = {
                                            if (notification.reminderId > 0) {
                                                navController.navigate(Routes.reminderDetailRoute(notification.reminderId))
                                            }
                                        }
                                    )
                                    if (notification != notifications.last()) {
                                        Divider(modifier = Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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