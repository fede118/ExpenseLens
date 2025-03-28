package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdExpenses
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class CommonMapperFunctionsKtTest {

    @Test
    fun `getTotalExpensesValue returns total`() {
        // Given
        val mockExpense1: Expense = mock()
        val mockExpense2: Expense = mock()
        whenever(mockExpense2.total).thenReturn(200.0)
        whenever(mockExpense1.total).thenReturn(100.0)
        val householdExpenses = HouseholdExpenses(
            householdInfo = mock(),
            monthOfExpenses = "June",
            expenses = listOf(mockExpense1, mockExpense2)
        )

        // When
        val result = householdExpenses.getTotalExpensesValue()

        // Then
        assertEquals(300.0f, result)
    }

    @Test
    fun `getSlicesByCategory returns slices`() {
        // Given
        val mockExpense1: Expense = mock()
        val mockExpense2: Expense = mock()
        whenever(mockExpense1.category).thenReturn("category1")
        whenever(mockExpense2.category).thenReturn("category2")
        whenever(mockExpense2.total).thenReturn(200.0)
        whenever(mockExpense1.total).thenReturn(100.0)
        val householdExpenses = HouseholdExpenses(
            householdInfo = mock(),
            monthOfExpenses = "June",
            expenses = listOf(mockExpense1,  mockExpense2)
        )

        // When
        val result = householdExpenses.getSlicesByCategory(300.0f)

        // Then
        assertEquals(2, result.size)
        assertEquals("category1 - 33.33%", result[0].label)
        assertEquals(100.0f, result[0].value)
        assertEquals("category2 - 66.67%", result[1].label)
        assertEquals(200.0f, result[1].value)
    }
}
