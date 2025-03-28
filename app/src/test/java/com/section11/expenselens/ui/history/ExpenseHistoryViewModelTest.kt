package com.section11.expenselens.ui.history

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseHistoryViewModelTest {

    private lateinit var viewModel: ExpenseHistoryViewModel
    private val householdUseCase: HouseholdUseCase = mock()
    private val signInUseCase: SignInUseCase = mock()
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
            Expense("Food", 100.0, mock(), userId, "ted","Dinner"),
            Expense("Transport", 50.0, mock(), userId, "ted", "Taxi")
        )
        val householdExpenses = HouseholdExpenses(
            userHouseHold,
            expenses
            )
        val mockUserData: UserData = mock()
        whenever(mockUserData.currentHouseholdId).thenReturn(householdId)
        whenever(mockUserData.id).thenReturn(userId)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        whenever(householdUseCase.getCurrentHousehold(userId)).thenReturn(householdExpenses)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(expenses, viewModel.uiState.first())
    }

    @Test
    fun `uiState remains empty when household does not exist`() = runTest {
        // Given
        whenever(householdUseCase.getCurrentHousehold("user123"))
            .thenReturn(null)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(emptyList<FirestoreExpense>(), viewModel.uiState.first())
    }

    @Test
    fun `uiState remains empty when user is null`() = runTest {
        // Given
        whenever(signInUseCase.getCurrentUser()).thenReturn(null)

        // When
        viewModel = ExpenseHistoryViewModel(householdUseCase, signInUseCase, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(emptyList<FirestoreExpense>(), viewModel.uiState.first())
    }
}
