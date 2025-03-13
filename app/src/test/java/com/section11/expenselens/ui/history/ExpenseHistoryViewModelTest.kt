package com.section11.expenselens.ui.history

import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel.ExpenseHistoryUiState.ShowExpenseHistory
import com.section11.expenselens.ui.history.event.ExpenseHistoryUpstreamEvent.OnExpenseHistoryItemDeleted
import com.section11.expenselens.ui.history.mapper.ExpenseHistoryUiMapper
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseHistoryViewModelTest {

    private lateinit var viewModel: ExpenseHistoryViewModel
    private val householdUseCase: HouseholdUseCase = mock()
    private val signInUseCase: SignInUseCase = mock()
    private val mapper: ExpenseHistoryUiMapper = mock()
    private val userData: UserData = mock()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(userData.id).thenReturn("user123")
        runTest {
            whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(userData))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState contains expenses when household exists`() = runTest {
        // Given
        val householdId = "household123"
        val userId = "user123"
        val userHouseHold = UserHousehold(householdId, "Test Household")
        val expenses = listOf(
            ExpenseHistoryUiItem("idFood", "Food", 100.0, "Mar 3 2025", userId, "ted", "Dinner"),
            ExpenseHistoryUiItem(
                "idTransport",
                "Transport",
                50.0,
                "Mar 3 2025",
                userId,
                "ted",
                "Taxi"
            )
        )
        val householdExpenses = HouseholdExpenses(
            userHouseHold,
            emptyList()
        )
        val mockUserData: UserData = mock()
        whenever(mockUserData.currentHouseholdId).thenReturn(householdId)
        whenever(mockUserData.id).thenReturn(userId)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        whenever(householdUseCase.getCurrentHousehold(userId)).thenReturn(householdExpenses)
        whenever(mapper.mapExpensesToUiItems(any())).thenReturn(expenses)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, mapper, dispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assert(state is ShowExpenseHistory)
        assertEquals(expenses, (state as ShowExpenseHistory).expenses)
    }

    @Test
    fun `uiState remains empty when household does not exist`() = runTest {
        // Given
        whenever(householdUseCase.getCurrentHousehold("user123"))
            .thenReturn(null)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, mapper, dispatcher)
        advanceUntilIdle()

        // Then
        assert(viewModel.uiState.first() is UiState.Error)
    }

    @Test
    fun `uiState remains empty when user is null`() = runTest {
        // Given
        whenever(signInUseCase.getCurrentUser()).thenReturn(null)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, mapper, dispatcher)
        advanceUntilIdle()

        // Then
        assert(viewModel.uiState.first() is UiState.Error)
    }

    @Test
    fun `on delete expense upstream event then mapper is called to delete expense and state updated`() = runTest {
        // Given
        val userId = "user123"
        val userHouseHold = UserHousehold("household123", "Test Household")
        val expenses = listOf(
            ExpenseHistoryUiItem("idFood", "Food", 100.0, "Mar 3 2025", userId, "ted", "Dinner"),
            ExpenseHistoryUiItem(
                "idTransport",
                "Transport",
                50.0,
                "Mar 3 2025",
                userId,
                "ted",
                "Taxi"
            )
        )
        val householdExpenses = HouseholdExpenses(
            userHouseHold,
            emptyList()
        )
        val mockUserData: UserData = mock()
        whenever(mockUserData.currentHouseholdId).thenReturn("household123")
        whenever(mockUserData.id).thenReturn(userId)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        whenever(householdUseCase.getCurrentHousehold(userId)).thenReturn(householdExpenses)
        whenever(mapper.mapExpensesToUiItems(any())).thenReturn(expenses)
        whenever(mapper.deleteExpenseFromList(any(), any()))
            .thenReturn(listOf(expenses[1]))
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, mapper, dispatcher)

        // When
        viewModel.onUpstreamEvent(OnExpenseHistoryItemDeleted("idFood"))
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assert(state is ShowExpenseHistory)
        assertEquals(1, (state as ShowExpenseHistory).expenses.size)
        assertEquals("idTransport", state.expenses[0].expenseId)
    }

    @Test
    fun `on delete failed then item is re entered to the list`() = runTest {
        // Given
        val userId = "user123"
        val userHouseHold = UserHousehold("household123", "Test Household")
        val expenses = listOf(
            ExpenseHistoryUiItem("idFood", "Food", 100.0, "Mar 3 2025", userId, "ted", "Dinner"),
            ExpenseHistoryUiItem(
                "idTransport",
                "Transport",
                50.0,
                "Mar 3 2025",
                userId,
                "ted",
                "Taxi"
            )
        )
        val householdExpenses = HouseholdExpenses(
            userHouseHold,
            emptyList()
        )
        val mockUserData: UserData = mock()
        whenever(mockUserData.currentHouseholdId).thenReturn("household123")
        whenever(mockUserData.id).thenReturn(userId)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        whenever(householdUseCase.getCurrentHousehold(userId)).thenReturn(householdExpenses)
        whenever(mapper.mapExpensesToUiItems(any())).thenReturn(expenses)
        whenever(mapper.deleteExpenseFromList(any(), any()))
            .thenReturn(listOf(expenses[1]))
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, mapper, dispatcher)
        whenever(householdUseCase.deleteExpenseFromHousehold(any(), any()))
            .thenReturn(Result.failure(Exception()))

        // When
        viewModel.onUpstreamEvent(OnExpenseHistoryItemDeleted("idFood"))
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assert(state is ShowExpenseHistory)
        assertEquals(2, (state as ShowExpenseHistory).expenses.size)
    }
}
