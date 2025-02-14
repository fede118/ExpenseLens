package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.DATE_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
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
        mapper = ExpenseReviewScreenUiMapper(resourceProvider)
        whenever(resourceProvider.getString(R.string.expense_review_screen_no_date))
            .thenReturn(noDateFallback)
    }

    @Test
    fun `mapExpenseInfoToUiModel should return correct UI model when expense information is provided`() {
        // Given
        val totalWithoutDollarSign = "100"
        val expenseInfo = SuggestedExpenseInformation(
            estimatedCategory = Category.HOME,
            total = "$${totalWithoutDollarSign}",
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
                    assertEquals(expenseInfo.date, it.value)// dollar sign removed
                }
                TOTAL -> {
                    assertEquals(totalTitle, it.title)
                    assertEquals(totalWithoutDollarSign, it.value)
                }
                ExpenseReviewSections.ADD_NOTE -> {
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
            total = "$200",
            date = "15/01/2025"
        )

        // When
        val result = mapper.mapExpenseInfoToUiModel(expenseInfo, extractedText = null)

        // Then
        assertEquals(ExpenseReviewSections.entries.size, result.reviewRows.size)
        val categoryRow = result.reviewRows.first { it.section == CATEGORY_SELECTION }
        assertEquals("Select a category", categoryRow.value) // Default fallback
    }

    @Test
    fun `mapExpenseInfoToUiModel should handle null expense info gracefully`() {
        val extractedText = "Some extracted text"

        // When
        val result = mapper.mapExpenseInfoToUiModel(null, extractedText)

        // Then
        assertEquals(extractedText, result.extractedText)
        result.reviewRows.forEach {
            when (it.section) {
                CATEGORY_SELECTION -> {
                    assertEquals(categoryTitle, it.title)
                    assertEquals(selectCategoryLabel, it.value) // fallback string i ui mapper
                }

                DATE_SELECTION -> {
                    assertEquals(datePickerTitle, it.title)
                    assertEquals(noDateFallback, it.value)
                }
                TOTAL -> {
                    assertEquals(totalTitle, it.title)
                    assertEquals("", it.value)
                }
                ExpenseReviewSections.ADD_NOTE -> {
                    assertEquals(noteTitle, it.title)
                    assert(it.value.isEmpty())
                }
            }
        }
    }
}
