package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ExpenseReviewScreenUiMapperTest {

    private val resourceProvider: ResourceProvider = mock()

    private lateinit var mapper: ExpenseReviewScreenUiMapper

    @Before
    fun setUp() {
        whenever(resourceProvider.getString(R.string.expense_review_screen_category_title))
            .thenReturn("Category")
        whenever(resourceProvider.getString(R.string.expense_review_screen_select_category))
            .thenReturn("Select a category")
        whenever(resourceProvider.getString(R.string.expense_review_screen_total_title))
            .thenReturn("Total")
        whenever(resourceProvider.getString(R.string.dollar_sign))
            .thenReturn("$")
        mapper = ExpenseReviewScreenUiMapper(resourceProvider)
    }

    @Test
    fun `mapExpenseInfoToUiModel should return correct UI model when expense information is provided`() {
        // Given
        val expenseInfo = ExpenseInformation(
            estimatedCategory = Category.HOME,
            total = "$100"
        )
        val extractedText = "Some extracted text"

        // When
        val result = mapper.mapExpenseInfoToUiModel(expenseInfo, extractedText)

        // Then
        assertEquals(extractedText, result.extractedText)
        assertEquals(2, result.reviewRows.size)

        val categoryRow = result.reviewRows[0]
        assertEquals("Category", categoryRow.title)
        assertEquals("Home", categoryRow.value)
        assert(categoryRow.type is DropdownMenu)

        val totalRow = result.reviewRows[1]
        assertEquals("Total", totalRow.title)
        assertEquals("100", totalRow.value) // Dollar sign removed
        assert(totalRow.type is ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput)
    }

    @Test
    fun `mapExpenseInfoToUiModel should return default category when estimatedCategory is null`() {
        // Given
        val expenseInfo = ExpenseInformation(
            estimatedCategory = null,
            total = "$200"
        )

        // When
        val result = mapper.mapExpenseInfoToUiModel(expenseInfo, extractedText = null)

        // Then
        assertEquals(2, result.reviewRows.size)

        val categoryRow = result.reviewRows[0]
        assertEquals("Select a category", categoryRow.value) // Default fallback

        val totalRow = result.reviewRows[1]
        assertEquals("200", totalRow.value) // Dollar sign removed
    }

    @Test
    fun `mapExpenseInfoToUiModel should handle null expense info gracefully`() {
        val expectedCategory = "Select a category"

        // When
        val result = mapper.mapExpenseInfoToUiModel(null, extractedText = "Extracted text")

        // Then
        assertEquals("Extracted text", result.extractedText)
        assertEquals(result.reviewRows.size, 2) // Rows are added as default, just with no value
        assertEquals(result.reviewRows[0].value, expectedCategory)
        assertEquals(result.reviewRows[1].value, "")
    }
}
