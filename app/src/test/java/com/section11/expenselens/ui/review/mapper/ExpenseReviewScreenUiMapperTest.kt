package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.ADD_NOTE
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.DATE_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
import com.section11.expenselens.ui.review.validator.InvalidExpenseCategoryException
import com.section11.expenselens.ui.review.validator.InvalidExpenseDateException
import com.section11.expenselens.ui.review.validator.InvalidExpenseTotalException
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ExpenseReviewScreenUiMapperTest {

    private val resourceProvider: ResourceProvider = mock()

    private lateinit var mapper: ExpenseReviewScreenUiMapper

    private val categoryTitle = "Category"
    private val selectCategoryLabel = "Select a category"
    private val totalTitle = "total"
    private val dollarSign = "$"
    private val datePickerTitle = "Date"
    private val noteTitle = "note"
    private val noDateFallback = "no date fallback"
    private val noCategoryFallback = "no category fallback"

    @Before
    fun setUp() {
        whenever(resourceProvider.getString(R.string.expense_review_screen_category_title))
            .thenReturn(categoryTitle)
        whenever(resourceProvider.getString(R.string.expense_review_screen_select_category))
            .thenReturn(selectCategoryLabel)
        whenever(resourceProvider.getString(R.string.expense_review_screen_total_title))
            .thenReturn(totalTitle)
        whenever(resourceProvider.getString(R.string.dollar_sign))
            .thenReturn(dollarSign)
        whenever(resourceProvider.getString(R.string.date_picker_title)).thenReturn(datePickerTitle)
        whenever(resourceProvider.getString(R.string.expense_review_screen_note_title))
            .thenReturn(noteTitle)
        whenever(resourceProvider.getString(R.string.expense_review_screen_no_date))
            .thenReturn(noDateFallback)
        whenever(resourceProvider.getString(R.string.expense_review_screen_select_category))
            .thenReturn(noCategoryFallback)
        mapper = ExpenseReviewScreenUiMapper(resourceProvider)
    }

    @Test
    fun `mapExpenseInfoToUiModel should return correct UI model when expense information is provided`() {
        // Given
        val expenseInfo = SuggestedExpenseInformation(
            estimatedCategory = Category.HOME,
            total = 100.0,
            date = "12/12/2024"
        )
        val extractedText = "Some extracted text"

        // When
        val result = mapper.mapExpenseInfoToUiModel(expenseInfo, extractedText)

        // Then
        assertEquals(extractedText, result.extractedText)
        assertEquals(ExpenseReviewSections.entries.size, result.reviewRows.size)

        result.reviewRows.forEach {
            when (it.section) {
                CATEGORY_SELECTION -> {
                    assertEquals(categoryTitle, it.title)
                    assertEquals(expenseInfo.estimatedCategory?.displayName, it.value)
                }

                DATE_SELECTION -> {
                    assertEquals(datePickerTitle, it.title)
                    assertEquals(expenseInfo.date, it.value)
                }
                TOTAL -> {
                    assertEquals(totalTitle, it.title)
                    assertEquals(expenseInfo.total.toString(), it.value)
                }
                ADD_NOTE -> {
                    assertEquals(noteTitle, it.title)
                    assert(it.value.isEmpty())
                }
            }
        }
    }

    @Test
    fun `mapExpenseInfoToUiModel should return default category when estimatedCategory is null`() {
        // Given
        val expenseInfo = SuggestedExpenseInformation(
            estimatedCategory = null,
            total = 200.00,
            date = "15/01/2025"
        )

        // When
        val result = mapper.mapExpenseInfoToUiModel(expenseInfo, extractedText = null)

        // Then
        assertEquals(ExpenseReviewSections.entries.size, result.reviewRows.size)
        val categoryRow = result.reviewRows.first { it.section == CATEGORY_SELECTION }
        assertEquals(noCategoryFallback, categoryRow.value)
    }

    @Test
    fun `getNoExpenseFoundMessageAndUiModel should return default info and error message`() {
        // Given
        val errorMessage = "error"
        whenever(resourceProvider.getString(R.string.expense_review_screen_no_expense_found))
            .thenReturn(errorMessage)

        // When
        val result = mapper.getNoExpenseFoundMessageAndUiModel()

        val message = result.first
        val expenseReviewUiModel = result.second

        // Then
        assertEquals(errorMessage, message)
        expenseReviewUiModel.reviewRows.forEach { row ->
            when(row.section) {
                CATEGORY_SELECTION -> {
                    assertEquals(noCategoryFallback, row.value)
                    assertEquals(categoryTitle, row.title)
                }
                DATE_SELECTION -> {
                    assertEquals(noDateFallback, row.value)
                    assertEquals(datePickerTitle, row.title)
                }
                TOTAL -> {
                    assertEquals("0.0", row.value)
                    assertEquals(totalTitle, row.title)
                }
                ADD_NOTE -> {
                    assertEquals(String(), row.value)
                    assertEquals(noteTitle, row.title)
                }
            }
        }
    }

    @Test
    fun `getErrorMessageFromExpenseValidationException should return correct category error message`() {
        val categoryException = InvalidExpenseCategoryException()
        val error = "error"
        whenever(resourceProvider.getString(R.string.expense_review_screen_error_in_category))
            .thenReturn(error)

        val result = mapper.getErrorMessageFromExpenseValidationException(categoryException)

        assertEquals(error, result)
    }

    @Test
    fun `getErrorMessageFromExpenseValidationException should return correct date error message`() {
        val dateException = InvalidExpenseDateException()
        val error = "error"
        whenever(resourceProvider.getString(R.string.expense_review_screen_error_in_date))
            .thenReturn(error)

        val result = mapper.getErrorMessageFromExpenseValidationException(dateException)

        assertEquals(error, result)
    }

    @Test
    fun `getErrorMessageFromExpenseValidationException should return correct total error message`() {
        val totalException = InvalidExpenseTotalException()
        val error = "error"
        whenever(resourceProvider.getString(R.string.expense_review_screen_error_in_total))
            .thenReturn(error)

        val result = mapper.getErrorMessageFromExpenseValidationException(totalException)

        assertEquals(error, result)
    }

    @Test
    fun `getErrorMessageFromExpenseValidationException should return correct generic error message`() {
        val randomError = IllegalArgumentException()
        val error = "error"
        whenever(resourceProvider.getString(R.string.expense_review_screen_error_when_submitting))
            .thenReturn(error)

        val result = mapper.getErrorMessageFromExpenseValidationException(randomError)

        assertEquals(error, result)
    }

    @Test
    fun `getEmptyExpenseReviewUiModel should return empty UI model`() {
        // When
        val result = mapper.getEmptyExpenseReviewUiModel()

        // Then
        assertEquals(ExpenseReviewSections.entries.size, result.reviewRows.size)
        result.reviewRows.forEach { row ->
            when(row.section) {
                CATEGORY_SELECTION -> {
                    assertEquals(noCategoryFallback, row.value)
                    assertEquals(categoryTitle, row.title)
                }
                DATE_SELECTION -> {
                    assertEquals(noDateFallback, row.value)
                    assertEquals(datePickerTitle, row.title)
                }
                TOTAL -> {
                    assertEquals("0.0", row.value)
                    assertEquals(totalTitle, row.title)
                }
                ADD_NOTE -> {
                    assertEquals(String(), row.value)
                    assertEquals(noteTitle, row.title)
                }
            }
        }
    }
}
