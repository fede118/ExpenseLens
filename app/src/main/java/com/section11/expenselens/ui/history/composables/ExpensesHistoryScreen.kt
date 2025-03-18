package com.section11.expenselens.ui.history.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.HandleDownstreamEvents
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel.ExpenseHistoryUiState.ShowExpenseHistory
import com.section11.expenselens.ui.history.event.ExpenseHistoryUpstreamEvent
import com.section11.expenselens.ui.history.event.ExpenseHistoryUpstreamEvent.OnExpenseHistoryItemDeleted
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UiState.Error
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun ExpensesHistoryScreen(
    expenseHistoryUiState: UiState,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    modifier: Modifier = Modifier,
    onEvent: (ExpenseHistoryUpstreamEvent) -> Unit
) {
    HandleDownstreamEvents(downstreamUiEvent)
    when(expenseHistoryUiState) {
        is ShowExpenseHistory -> ExpenseHistoryList(
            expenseHistoryUiState.expenses,
            onEvent,
            modifier
        )
        is Error -> {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = expenseHistoryUiState.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryList(
    expenses: List<ExpenseHistoryUiItem>,
    onEvent: (ExpenseHistoryUpstreamEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimens.current
    val coroutineScope = rememberCoroutineScope()

    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.expenses_history_empty_state_string),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(dimens.m2)
        ) {
            item {
                Text(
                    modifier = Modifier.statusBarsPadding(),
                    text = stringResource(R.string.expenses_history_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(dimens.m1))
            }

            items(expenses, key = { it.expenseId }) { expense ->
                val dismissState = rememberSwipeToDismissBoxState()

                if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                    SideEffect {
                        coroutineScope.launch {
                            dismissState.dismiss(SwipeToDismissBoxValue.StartToEnd)
                            onEvent(OnExpenseHistoryItemDeleted(expense.expenseId))
                        }
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Icon(
                            painterResource(id = R.drawable.trash),
                            contentDescription = "Delete item",
                            modifier = Modifier
                                .size(dimens.m3)
                                .align(Alignment.CenterVertically)
                        )
                    },
                ) {
                    ExpenseItem(expense = expense)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: ExpenseHistoryUiItem) {
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = dimens.m1)
    ) {
        Text(text = expense.category, style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(R.string.expense_history_total_prefix, expense.total),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.expense_history_date_prefix, expense.date),
            style = MaterialTheme.typography.bodySmall
        )
        expense.userDisplayName?.let { userName ->
            Text(
                text = stringResource(R.string.expense_history_submitted_by, userName),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (expense.note?.isNotEmpty() == true) {
            Text(
                text = stringResource(R.string.expense_history_note_prefix, expense.note),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@DarkAndLightPreviews
@Composable
fun ExpenseHistoryPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        ExpensesHistoryScreen(
            expenseHistoryUiState = ShowExpenseHistory(fakeRepo.getExpenseHistoryList()),
            downstreamUiEvent = MutableSharedFlow(),
            onEvent = {}
        )
    }
}
