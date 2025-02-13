package com.section11.expenselens.domain.usecase

import com.google.firebase.Timestamp
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.repository.ExpensesRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

private const val TEST_HOUSEHOLD_NAME = "Test Household"

@ExperimentalCoroutinesApi
class StoreExpenseUseCaseTest {

    private lateinit var useCase: StoreExpenseUseCase
    private val mockRepository: ExpensesRepository = mock()

    @Before
    fun setup() {
        useCase = StoreExpenseUseCase(mockRepository)
    }

    @Test
    fun `getCurrentHouseholdIdAndName returns existing household ID`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        whenever(mockRepository.getHousehold(TEST_HOUSEHOLD_NAME)).thenReturn(householdId)

        // When
        val result = useCase.getCurrentHouseholdIdAndName(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(householdId to TEST_HOUSEHOLD_NAME, result.getOrNull())
    }

    @Test
    fun `getCurrentHouseholdIdAndName creates household if not found`() = runTest {
        // Given
        val userId = "user123"
        val newHouseholdId = "household789"
        whenever(mockRepository.getHousehold(TEST_HOUSEHOLD_NAME)).thenReturn(null)
        whenever(mockRepository.createHousehold(TEST_HOUSEHOLD_NAME, userId))
            .thenReturn(Result.success(newHouseholdId to TEST_HOUSEHOLD_NAME))

        // When
        val result = useCase.getCurrentHouseholdIdAndName(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(newHouseholdId to TEST_HOUSEHOLD_NAME, result.getOrNull())
    }

    @Test
    fun `addExpense successfully adds expense when household ID is provided`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        whenever(mockRepository.addExpenseToHousehold(userId, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpense(userId, expense, householdId)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `addExpense retrieves household ID before adding expense`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        whenever(mockRepository.getHousehold(TEST_HOUSEHOLD_NAME)).thenReturn(householdId)
        whenever(mockRepository.addExpenseToHousehold(userId, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpense(userId, expense)

        // Then
        assertTrue(result.isSuccess)
        verify(mockRepository).getHousehold(TEST_HOUSEHOLD_NAME) // Ensure household was retrieved
    }

    @Test
    fun `addExpense fails if no household is found`() = runTest {
        // Given
        val userId = "user123"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        whenever(mockRepository.getHousehold(TEST_HOUSEHOLD_NAME)).thenReturn(null)

        // When
        val result = useCase.addExpense(userId, expense)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HouseholdNotFoundException)
    }

    @Test
    fun `getAllExpensesFromHousehold returns list of expenses`() = runTest {
        // Given
        val householdId = "household456"
        val expenses = listOf(
            FirestoreExpense(
                total = 100.0,
                category = "Home",
                date = Timestamp(Date()),
                userId = "user123",
                note = "Dinner",
                distributedExpense = emptyMap()
            )
        )

        whenever(mockRepository.getAllExpensesFromHousehold(householdId))
            .thenReturn(Result.success(expenses))

        // When
        val result = useCase.getAllExpensesFromHousehold(householdId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expenses, result.getOrNull())
    }
}
