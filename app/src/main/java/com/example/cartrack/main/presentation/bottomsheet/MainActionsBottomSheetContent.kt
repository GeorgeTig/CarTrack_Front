package com.example.cartrack.main.presentation.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Iterează prin toate acțiunile, cu excepția ultimei dacă este stilizată ca buton
        // și vrem să o afișăm separat după un spațiu mai mare.
        val normalActions = actions.filterNot { it.isStyledAsButton && it == actions.lastOrNull() }
        val lastButtonAction = actions.lastOrNull()?.takeIf { it.isStyledAsButton }

        normalActions.forEachIndexed { index, action ->
            BottomSheetActionItem(
                action = action,
                onClick = { onActionClick(action) }
            )
            // Adaugă divider între acțiunile normale
            if (index < normalActions.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Dacă există acțiuni normale ȘI un buton final, adaugă un spațiu mai mare
        if (normalActions.isNotEmpty() && lastButtonAction != null) {
            Spacer(modifier = Modifier.height(20.dp)) // Spațiu mai mare înainte de butonul final
        } else if (normalActions.isEmpty() && lastButtonAction != null) {
            // Dacă DOAR butonul final există, adaugă un spațiu mic sus
            Spacer(modifier = Modifier.height(8.dp))
        }


        // Afișează ultima acțiune dacă este stilizată ca buton
        lastButtonAction?.let { action ->
            BottomSheetActionItem(
                action = action,
                onClick = { onActionClick(action) },
                // Adaugă un padding sus dacă nu sunt alte acțiuni înainte
                modifier = if (normalActions.isEmpty()) Modifier.padding(top = 8.dp) else Modifier
            )
        }
        // Nu mai e nevoie de un Spacer mare la finalul Column aici, padding-ul de jos al Column se ocupă
    }
}