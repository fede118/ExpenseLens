package com.section11.expenselens.ui.navigation.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.ui.history.composables.ExpensesHistoryScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ExpenseHistoryRoute(
    uiState: StateFlow<List<FirestoreExpense>>,
    modifier: Modifier = Modifier
) {
    val expenses by uiState.collectAsState()

    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            Text(
                "No expenses found",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        ExpensesHistoryScreen(expenses, modifier)
    }
}
