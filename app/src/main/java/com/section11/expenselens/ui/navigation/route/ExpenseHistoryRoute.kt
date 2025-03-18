package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel
import com.section11.expenselens.ui.history.composables.ExpensesHistoryScreen

@Composable
fun ExpenseHistoryRoute(modifier: Modifier = Modifier) {
    val expenseHistoryViewModel = hiltViewModel<ExpenseHistoryViewModel>()
    val expenseHistoryUiState by expenseHistoryViewModel.uiState.collectAsState()

    ExpensesHistoryScreen(expenseHistoryUiState, expenseHistoryViewModel.uiEvent,  modifier) { event ->
        expenseHistoryViewModel.onUpstreamEvent(event)
    }
}
