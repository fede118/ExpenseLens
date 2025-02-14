package com.section11.expenselens.ui.history


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.usecase.StoreExpenseUseCase
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
    private val storeExpensesUseCase: StoreExpenseUseCase = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseUser: FirebaseUser = mock()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("user123")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState contains expenses when household exists`() = runTest {
        // Given
        val householdId = "household123"
        val expenses = listOf(
            FirestoreExpense("Food", 100.0, mock(), "user123", "Dinner"),
            FirestoreExpense("Transport", 50.0, mock(), "user123", "Taxi")
        )

        whenever(storeExpensesUseCase.getCurrentHouseholdIdAndName("user123"))
            .thenReturn(Result.success(householdId to "Test Household"))
        whenever(storeExpensesUseCase.getAllExpensesFromHousehold(householdId))
            .thenReturn(Result.success(expenses))

        // When
        viewModel = ExpenseHistoryViewModel(storeExpensesUseCase, firebaseAuth, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(expenses, viewModel.uiState.first())
    }

    @Test
    fun `uiState remains empty when household does not exist`() = runTest {
        // Given
        whenever(storeExpensesUseCase.getCurrentHouseholdIdAndName("user123"))
            .thenReturn(Result.failure(Exception("Household not found")))

        // When
        viewModel = ExpenseHistoryViewModel(storeExpensesUseCase, firebaseAuth, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(emptyList<FirestoreExpense>(), viewModel.uiState.first())
    }

    @Test
    fun `uiState remains empty when user is null`() = runTest {
        // Given
        whenever(firebaseAuth.currentUser).thenReturn(null)

        // When
        viewModel = ExpenseHistoryViewModel(storeExpensesUseCase, firebaseAuth, dispatcher)
        advanceUntilIdle()

        // Then
        assertEquals(emptyList<FirestoreExpense>(), viewModel.uiState.first())
    }
}
