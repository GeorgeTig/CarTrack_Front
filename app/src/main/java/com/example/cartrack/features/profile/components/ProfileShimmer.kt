package com.example.cartrack.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.ui.components.ShimmerPlaceholder
import com.valentinilk.shimmer.shimmer

@Composable
fun ProfileScreenShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
            .shimmer(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Shimmer
        ShimmerPlaceholder(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(16.dp))
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerPlaceholder(modifier = Modifier.width(40.dp).height(24.dp))
                Spacer(Modifier.height(4.dp))
                ShimmerPlaceholder(modifier = Modifier.width(80.dp).height(16.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerPlaceholder(modifier = Modifier.width(40.dp).height(24.dp))
                Spacer(Modifier.height(4.dp))
                ShimmerPlaceholder(modifier = Modifier.width(50.dp).height(16.dp))
            }
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp))

        // Garage Shimmer
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(22.dp)
            )
            Spacer(Modifier.height(12.dp))
            repeat(3) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .padding(vertical = 6.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.large
                        )
                )
            }
        }
    }
}