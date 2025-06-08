package com.example.cartrack.features.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.ui.components.ShimmerPlaceholder
import com.valentinilk.shimmer.shimmer

@Composable
fun NotificationListShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.shimmer()) {
        repeat(6) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.8f).height(20.dp))
                    ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                }
            }
        }
    }
}