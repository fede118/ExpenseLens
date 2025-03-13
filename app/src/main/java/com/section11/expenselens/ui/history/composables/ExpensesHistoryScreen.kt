package com.section11.expenselens.ui.history.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.framework.utils.toFormattedString
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview

// TODO UI model needed, we are using a domain object
@Composable
fun ExpensesHistoryScreen(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimens.current

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

            items(expenses) { expense ->
                ExpenseItem(expense = expense)
                HorizontalDivider()
            }
        }
    }
}

// Todo move labels and formatting to uiModel
@Composable
fun ExpenseItem(expense: Expense) {
    val dimens = LocalDimens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.m1)
    ) {
        Text(text = expense.category, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Amount: $${expense.total}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Date: ${expense.date.toFormattedString()}",
            style = MaterialTheme.typography.bodySmall
        )
        expense.userDisplayName?.let { userName ->
            Text(
                text = "Submitted by: $userName",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (expense.note?.isNotEmpty() == true) {
            Text(
                text = "Note: ${expense.note}",
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
            expenses = fakeRepo.getExpenseHistoryList(),
        )
    }
}
