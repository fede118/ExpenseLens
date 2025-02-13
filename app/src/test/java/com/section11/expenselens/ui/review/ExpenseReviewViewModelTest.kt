package com.section11.expenselens.ui.review

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.usecase.StoreExpenseUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val UNAUTHENTICATED_ERROR = "Authentication Error occurred, please sign in again"

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseReviewViewModelTest {
    
    private val expenseReviewUiMapper: ExpenseReviewScreenUiMapper = mock()
    private val storeExpenseUseCase: StoreExpenseUseCase = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val navigationManager: NavigationManager = mock()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ExpenseReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        viewModel = ExpenseReviewViewModel(
            expenseReviewUiMapper,
            storeExpenseUseCase,
            firebaseAuth,
            navigationManager,
            dispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should set ShowExpenseReview state with mapped UI model`() = runTest {
        // Given
        val expenseInfo = SuggestedExpenseInformation(estimatedCategory = Category.HOME, total = "$100")
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

    @Test
    fun `on upstream event ExpenseSubmitted should add expense`() = runTest {
        // Given
        val expenseReviewUiModel = ExpenseReviewUiModel(
            "Extracted text",
            getListOfRows()
        )
        val expense = mock<ConsolidatedExpenseInformation>()
        mockFirebaseUserSuccess()
        whenever(expenseReviewUiMapper.toConsolidatedExpense(any())).thenReturn(expense)
        whenever(storeExpenseUseCase.addExpense(any(), any(), anyOrNull())).thenReturn(Result.success(Unit))

        viewModel.onUpstreamEvent(ExpenseSubmitted(expenseReviewUiModel))

        advanceUntilIdle()
        verify(storeExpenseUseCase).addExpense("user_id", expense)
        verify(navigationManager).navigate(NavigationManager.NavigationEvent.NavigateHome)
    }

    @Test
    fun `on upstream event ExpenseSubmitted if no user id then should show error`() = runTest {
        // Given
        val expenseReviewUiModel = ExpenseReviewUiModel(
            "Extracted text",
            getListOfRows()
        )
        val expense = mock<ConsolidatedExpenseInformation>()
        whenever(expenseReviewUiMapper.toConsolidatedExpense(any())).thenReturn(expense)
        whenever(storeExpenseUseCase.addExpense(any(), any(), anyOrNull())).thenReturn(Result.success(Unit))

        // Since this is a cold flow we need to start the collection before actually calling the viewModel method
        val job = launch {
            viewModel.uiEvent.collectIndexed { index, value ->
                if (index == 0) assert((value as? Loading)?.isLoading == true)
                if (index == 1) {
                    assert((value as? ShowSnackBar)?.message == UNAUTHENTICATED_ERROR)
                }
                cancel() // Cancel the coroutine after receiving the expected event
            }
        }

        viewModel.onUpstreamEvent(ExpenseSubmitted(expenseReviewUiModel))
        advanceUntilIdle()

        job.join() // Ensure the coroutine completes
        verify(navigationManager).navigate(NavigateHome)
    }

    private fun mockFirebaseUserSuccess(id: String = "user_id") {
        val mockUser: FirebaseUser = mock()
        whenever(mockUser.uid).thenReturn(id)
        whenever(firebaseAuth.currentUser).thenReturn(mockUser)
    }

    private fun getListOfRows(): List<ReviewRow> {
        return listOf(
            ReviewRow(
                id = "category",
                title = "Category",
                value = "Home",
                type = DropdownMenu(emptyList())
            ),
            ReviewRow(
                id = "total",
                title = "Total",
                value = "100",
                type = TextInput
            )
        )
    }
}
