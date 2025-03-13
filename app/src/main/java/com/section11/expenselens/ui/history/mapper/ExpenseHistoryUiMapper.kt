package com.section11.expenselens.ui.history.mapper

import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.framework.utils.toFormattedString
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import com.section11.expenselens.ui.utils.formatToTwoDecimal

class ExpenseHistoryUiMapper {

    fun mapExpensesToUiItems(expenses: List<Expense>): List<ExpenseHistoryUiItem> {
        return expenses.map {
            ExpenseHistoryUiItem(
                expenseId = it.expenseId,
                category = it.category,
                total = it.total.formatToTwoDecimal(),
                date = it.date.toFormattedString(),
                userId = it.userId,
                userDisplayName = it.userDisplayName,
                note = it.note,
                distributedExpense = it.distributedExpense
            )
        }
    }

    fun deleteExpenseFromList(
        expenses: List<ExpenseHistoryUiItem>,
        expenseId: String
    ): List<ExpenseHistoryUiItem> {
        return expenses.filter { it.expenseId != expenseId }
    }
}
