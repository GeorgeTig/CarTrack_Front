package com.example.cartrack.main.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MainActionsBottomSheetContent(
    actions: List<BottomSheetAction>,
    onActionClick: (BottomSheetAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val normalActions = actions.filterNot { it.isStyledAsButton }
        val buttonActions = actions.filter { it.isStyledAsButton }

        normalActions.forEachIndexed { index, action ->
            BottomSheetActionItem(action = action, onClick = { onActionClick(action) })
            if (index < normalActions.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (normalActions.isNotEmpty() && buttonActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
        }

        buttonActions.forEach { action ->
            BottomSheetActionItem(action = action, onClick = { onActionClick(action) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BottomSheetActionItem(
    action: BottomSheetAction,
    onClick: () -> Unit,
) {
    if (action.isStyledAsButton) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(action.icon, null, Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(action.title, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(action.icon, action.title, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(action.title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}