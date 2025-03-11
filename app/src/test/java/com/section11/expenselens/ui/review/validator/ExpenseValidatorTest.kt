package com.section11.expenselens.ui.review.validator

import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.ADD_NOTE
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.DATE_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseValidatorTest {

    private val validator = ExpenseValidator()

    @Test
    fun `validateExpense returns success when all fields are valid`() {
        // Given
        val expense = createValidExpense()

        // When
        val result = validator.validateExpense(expense)

        // Then
        result.onSuccess { info ->
            assertEquals(100.0, info.total, 0.0)
            assertEquals(Category.HOME, info.category)
            assertNotNull(info.date)
            assertEquals("Dinner with friends", info.note)
        }
    }

    @Test
    fun `validateExpense returns failure when total is missing`() {
        // Given
        val expense = createValidExpense().copyWithout(TOTAL)

        // When
        val result = validator.validateExpense(expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseTotalException)
    }

    @Test
    fun `validateExpense returns failure when total is not a number`() {
        // Given
        val expense = createValidExpense().copyReplacing(TOTAL, "abc")

        // When
        val result = validator.validateExpense(expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseTotalException)
    }

    @Test
    fun `validateExpense returns failure when category is missing`() {
        // Given
        val expense = createValidExpense().copyWithout(CATEGORY_SELECTION)

        // When
        val result = validator.validateExpense(expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseCategoryException)
    }

    @Test
    fun `validateExpense returns failure when category is invalid`() {
        // Given
        val expense = createValidExpense().copyReplacing(CATEGORY_SELECTION, "Unknown Category")

        // When
        val result = validator.validateExpense(expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseCategoryException)
    }

    @Test
    fun `validateExpense returns failure when date is missing`() {
        val expense = createValidExpense().copyWithout(DATE_SELECTION)
        val result = validator.validateExpense(expense)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseDateException)
    }

    @Test
    fun `validateExpense returns failure when date is invalid`() {
        // Given
        val expense = createValidExpense().copyReplacing(DATE_SELECTION, "invalid-date")

        // When
        val result = validator.validateExpense(expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidExpenseDateException)
    }

    @Test
    fun `validateExpense sets note to empty string when not provided`() {
        // Given
        val expense = createValidExpense().copyWithout(ADD_NOTE)

        // When
        val result = validator.validateExpense(expense)

        // Then
        result.onSuccess { info ->
            assertEquals("", info.note)
        }
    }

    private fun createValidExpense(): ExpenseSubmitted {
        return ExpenseSubmitted(
            expenseReviewUiModel = ExpenseReviewUiModel(
                reviewRows = listOf(
                    ReviewRow(CATEGORY_SELECTION, "category section", Category.HOME.displayName),
                    ReviewRow(DATE_SELECTION, "date section", "Feb 2 2025"),
                    ReviewRow(TOTAL, "total section", "100.0"),
                    ReviewRow(ADD_NOTE, "note section", "Dinner with friends")
                ),
                extractedText = null
            )
        )
    }

    private fun ExpenseSubmitted.copyWithout(section: ExpenseReviewSections): ExpenseSubmitted {
        return copy(
            expenseReviewUiModel = expenseReviewUiModel.copy(
                reviewRows = expenseReviewUiModel.reviewRows.filterNot { it.section == section }
            )
        )
    }

    private fun ExpenseSubmitted.copyReplacing(
        section: ExpenseReviewSections,
        newValue: String
    ): ExpenseSubmitted {
        return copy(
            expenseReviewUiModel = expenseReviewUiModel.copy(
                reviewRows = expenseReviewUiModel.reviewRows.map {
                    if (it.section == section) it.copy(value = newValue) else it
                }
            )
        )
    }
}
