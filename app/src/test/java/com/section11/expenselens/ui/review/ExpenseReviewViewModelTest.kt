package com.section11.expenselens.ui.review

import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseReviewViewModelTest {
    
    private val expenseReviewUiMapper: ExpenseReviewScreenUiMapper = mock()

    private lateinit var viewModel: ExpenseReviewViewModel

    @Before
    fun setUp() {
        viewModel = ExpenseReviewViewModel(expenseReviewUiMapper)
    }

    @Test
    fun `init should set ShowExpenseReview state with mapped UI model`() = runTest {
        // Given
        val expenseInfo = ExpenseInformation(estimatedCategory = Category.HOME, total = "$100")
        val extractedText = "Some extracted text"
        val expectedUiModel = ExpenseReviewUiModel(extractedText, emptyList())

        whenever(expenseReviewUiMapper.mapExpenseInfoToUiModel(expenseInfo, extractedText))
            .thenReturn(expectedUiModel)

        // When
        viewModel.init(expenseInfo, extractedText)
        advanceUntilIdle()

        // Then
        val result = viewModel.uiState.value
        assertEquals(result, ShowExpenseReview(expectedUiModel))
    }

    @Test
    fun `init with null expenseInfo should set ShowExpenseReview`() = runTest {
        val expectedExtractedText = "Extracted text"
        whenever(
            expenseReviewUiMapper.mapExpenseInfoToUiModel(null, expectedExtractedText)
        ).thenReturn(mock())

        // When
        viewModel.init(null, expectedExtractedText)

        // Then
        verify(expenseReviewUiMapper)
            .mapExpenseInfoToUiModel(null, expectedExtractedText)
    }

    @Test
    fun `onUpstreamEvent UserInputEvent should update review row value`() = runTest {
        // Given
        val extractedText = "Extracted text"
        val initialReviewRow = ReviewRow(
            id = "category",
            title = "Category",
            value = "Home",
            type = DropdownMenu(emptyList())
        )
        val initialUiModel = ExpenseReviewUiModel(extractedText, listOf(initialReviewRow))
        val expectedUpdatedRow = initialReviewRow.copy(value = "Transportation")
        val expectedUiModel = initialUiModel.copy(reviewRows = listOf(expectedUpdatedRow))
        whenever(
            expenseReviewUiMapper.mapExpenseInfoToUiModel(null, extractedText)
        ).thenReturn(initialUiModel)

        viewModel.init(null, "Extracted text")
        viewModel.onUpstreamEvent(
            UserInputEvent(
                "category",
                "Transportation"
            )
        )

        // Then
        val result = viewModel.uiState.value
        assertEquals(result, ShowExpenseReview(expectedUiModel))
    }

    @Test
    fun `onUpstreamEvent UserInputEvent should not modify other rows`() = runTest {
        // Given
        val extractedText = "Extracted text"
        val reviewRow1 = ReviewRow(
            id = "category",
            title = "Category",
            value = "Home",
            type = DropdownMenu(emptyList())
        )
        val reviewRow2 = ReviewRow(
            id = "total",
            title = "Total",
            value = "100",
            type = TextInput
        )
        val initialUiModel = ExpenseReviewUiModel(
            "Extracted text",
            listOf(reviewRow1, reviewRow2)
        )

        val expectedUpdatedRow1 = reviewRow1.copy(value = "Transportation")
        val expectedUiModel = initialUiModel.copy(
            reviewRows = listOf(expectedUpdatedRow1, reviewRow2)
        )
        whenever(
            expenseReviewUiMapper.mapExpenseInfoToUiModel(null, extractedText)
        ).thenReturn(initialUiModel)

        viewModel.init(null, extractedText)
        viewModel.onUpstreamEvent(UserInputEvent("category", "Transportation"))

        // Then
        val result = viewModel.uiState.value
        assertEquals(result, ShowExpenseReview(expectedUiModel))
    }
}
