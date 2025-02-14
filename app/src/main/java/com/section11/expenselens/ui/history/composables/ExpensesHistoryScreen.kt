package com.section11.expenselens.ui.history.composables

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.framework.utils.toFormattedString
import com.section11.expenselens.ui.theme.LocalDimens

@Composable
fun ExpensesHistoryScreen(
    expenses: List<FirestoreExpense>,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimens.current

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

// move labels and formatting to uiModel
@Composable
fun ExpenseItem(expense: FirestoreExpense) {
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
        expense.note?.let {
            Text(
                text = "Note: $it",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
