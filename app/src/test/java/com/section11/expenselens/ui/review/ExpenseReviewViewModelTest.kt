package com.section11.expenselens.ui.review

import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.validator.ExpenseValidator
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private const val UNAUTHENTICATED_ERROR = "Authentication Error occurred, please sign in again"

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseReviewViewModelTest {

    private val expenseReviewUiMapper: ExpenseReviewScreenUiMapper = mock()
    private val householdUseCase: HouseholdUseCase = mock()
    private val signInUseCase: SignInUseCase = mock()
    private val navigationManager: NavigationManager = mock()
    private val expenseValidator: ExpenseValidator = mock()
    private val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: ExpenseReviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        viewModel = ExpenseReviewViewModel(
            expenseReviewUiMapper,
            householdUseCase,
            signInUseCase,
            navigationManager,
            expenseValidator,
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
        val expenseInfo = SuggestedExpenseInformation(
            estimatedCategory = Category.HOME, total = 100.00,
            date = "12/12/2021"
        )
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
    fun `init with null expenseInfo should set ShowExpenseReview with default values`() = runTest {
        val expectedExtractedText = "Extracted text"
        val errorMessage = "errorMessage"
        val expenseReviewUiModel = ExpenseReviewUiModel(
            expectedExtractedText,
            getListOfRows()
        )
        whenever(
            expenseReviewUiMapper.getNoExpenseFoundMessageAndUiModel()
        ).thenReturn(errorMessage to expenseReviewUiModel)


        val job = launch {
            viewModel.uiEvent.collectIndexed { _, value ->
                assert(value is ShowSnackBar)
                assertEquals((value as ShowSnackBar).message, errorMessage)
                cancel() // Cancel the coroutine after receiving the expected event
            }
        }
        // When
        viewModel.init(null, expectedExtractedText)

        job.join() // Ensure the coroutine completes

        verify(expenseReviewUiMapper).getNoExpenseFoundMessageAndUiModel()
    }

    @Test
    fun `onUpstreamEvent UserInputEvent should update review row value`() = runTest {
        // Given
        val extractedText = "Extracted text"
        val initialReviewRow = ReviewRow(
            section = CATEGORY_SELECTION,
            title = "Category",
            value = "Home"
        )
        val initialUiModel = ExpenseReviewUiModel(extractedText, listOf(initialReviewRow))
        val expectedUpdatedRow = initialReviewRow.copy(value = "Transportation")
        val expectedUiModel = initialUiModel.copy(reviewRows = listOf(expectedUpdatedRow))
        whenever(
            expenseReviewUiMapper.getNoExpenseFoundMessageAndUiModel()
        ).thenReturn("errorMessage" to initialUiModel)

        viewModel.init(null, "Extracted text")
        viewModel.onUpstreamEvent(
            UserInputEvent(
                CATEGORY_SELECTION,
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
            section = CATEGORY_SELECTION,
            title = "Category",
            value = "Home"
        )
        val reviewRow2 = ReviewRow(
            section = TOTAL,
            title = "Total",
            value = "100"
        )
        val initialUiModel = ExpenseReviewUiModel(
            "Extracted text",
            listOf(reviewRow1, reviewRow2)
        )
        whenever(
            expenseReviewUiMapper.getNoExpenseFoundMessageAndUiModel()
        ).thenReturn("errorMessage" to initialUiModel)
        val expectedUpdatedRow1 = reviewRow1.copy(value = "Transportation")
        val expectedUiModel = initialUiModel.copy(
            reviewRows = listOf(expectedUpdatedRow1, reviewRow2)
        )

        viewModel.init(null, extractedText)
        viewModel.onUpstreamEvent(UserInputEvent(CATEGORY_SELECTION, "Transportation"))

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
        val userMock = mockUserData()
        whenever(expenseValidator.validateExpense(any())).thenReturn(Result.success(expense))
        whenever(householdUseCase.addExpenseToCurrentHousehold(any(), any())).thenReturn(Result.success(Unit))

        viewModel.onUpstreamEvent(ExpenseSubmitted(expenseReviewUiModel))

        advanceUntilIdle()
        verify(householdUseCase).addExpenseToCurrentHousehold(userMock, expense)
        verify(navigationManager).navigate(NavigateHome(true))
    }

    @Test
    fun `on upstream event ExpenseSubmitted if no user id then should show error`() = runTest {
        // Given
        val expenseReviewUiModel = ExpenseReviewUiModel(
            "Extracted text",
            getListOfRows()
        )
        val expense = mock<ConsolidatedExpenseInformation>()
        whenever(expenseValidator.validateExpense(any())).thenReturn(Result.success(expense))
        whenever(householdUseCase.addExpenseToCurrentHousehold(any(), any())).thenReturn(Result.success(Unit))

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
        verify(navigationManager).navigate(NavigateHome(false))
    }

    private fun mockUserData(id: String = "user_id", displayName: String = "display_name"): UserData {
        val mockUser: UserData = mock()
        whenever(mockUser.id).thenReturn(id)
        whenever(mockUser.displayName).thenReturn(displayName)
        runTest {
            whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUser))
        }
        return mockUser
    }

    private fun getListOfRows(): List<ReviewRow> {
        return listOf(
            ReviewRow(
                section = CATEGORY_SELECTION,
                title = "Category",
                value = "Home"
            ),
            ReviewRow(
                section = TOTAL,
                title = "Total",
                value = "100"
            )
        )
    }
}
