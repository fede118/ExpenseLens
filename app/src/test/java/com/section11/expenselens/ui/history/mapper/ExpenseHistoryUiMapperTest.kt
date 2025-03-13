package com.section11.expenselens.ui.history.mapper

import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.framework.utils.toFormattedString
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel.ExpenseHistoryUiState.ShowExpenseHistory
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.util.Date

class ExpenseHistoryUiMapperTest {

    private val mapper = ExpenseHistoryUiMapper()

    @Test
    fun `when mapper gets called with valid data, then it should return valid data`() {
        // Given
        val expense = listOf(
            Expense(
                expenseId = "1",
                category = "Grocery",
                total = 100.0,
                date = Date(),
                userId = "1",
                userDisplayName = "John Doe",
                note = "Grocery shopping",
                distributedExpense = mapOf("1" to 50.0, "2" to 50.0)
            )
        )

        // When
        val result = mapper.mapExpensesToUiItems(expense)

        // Then
        val expected = ExpenseHistoryUiItem(
            expenseId = "1",
            category = "Grocery",
            total = 100.0,
            date = Date().toFormattedString(),
            userId = "1",
            userDisplayName = "John Doe",
            note = "Grocery shopping",
            distributedExpense = mapOf("1" to 50.0, "2" to 50.0)
        )
        assertEquals(expected, result.first())
    }

    @Test
    fun `when deleteExpenseFromList gets called with valid data, then it should return valid data`() {
        // Given
        val currentState = ShowExpenseHistory(
            expenses = listOf(
                ExpenseHistoryUiItem(
                    expenseId = "1",
                    category = "Grocery",
                    total = 100.0,
                    date = Date().toFormattedString(),
                    userId = "1",
                    userDisplayName = "John Doe",
                    note = "Grocery shopping",
                    distributedExpense = mapOf("1" to 50.0, "2" to 50.0)
                )
            )
        )

        // When
        val result = mapper.deleteExpenseFromList(currentState.expenses, "1")

        // Then
        assertEquals(emptyList<ExpenseHistoryUiItem>(), result)
    }
}
