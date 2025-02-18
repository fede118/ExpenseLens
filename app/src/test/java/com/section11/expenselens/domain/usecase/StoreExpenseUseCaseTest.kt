package com.section11.expenselens.domain.usecase

import com.google.firebase.Timestamp
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.ExpensesRepository
import com.section11.expenselens.domain.repository.UserHouseholdsRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

private const val TEST_HOUSEHOLD_NAME = "Test Household"

@ExperimentalCoroutinesApi
class StoreExpenseUseCaseTest {

    private lateinit var useCase: StoreExpenseUseCase
    private val expensesRepository: ExpensesRepository = mock()
    private val usersHouseholdRepository: UserHouseholdsRepository = mock()

    @Before
    fun setup() {
        useCase = StoreExpenseUseCase(expensesRepository, usersHouseholdRepository)
    }

    @Test
    fun `getCurrentHousehold returns existing household ID`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val households = listOf(UserHousehold(householdId, TEST_HOUSEHOLD_NAME))
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)

        // When
        val result = useCase.getCurrentHousehold(userId)

        // Then
        assertEquals(householdId, result?.id)
    }

    @Test
    fun `addExpense successfully adds expense when household ID is provided`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )
        val households = listOf(UserHousehold(householdId, TEST_HOUSEHOLD_NAME))
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)
        whenever(expensesRepository.addExpenseToHousehold(userMock, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpense(userMock, expense)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `addExpense retrieves household ID before adding expense`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        val households = listOf(UserHousehold(householdId, TEST_HOUSEHOLD_NAME))
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)
        whenever(expensesRepository.addExpenseToHousehold(userMock, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpense(userMock, expense)

        // Then
        assertTrue(result.isSuccess)
        verify(usersHouseholdRepository).getUserHouseholds(userId)
    }

    @Test
    fun `addExpense fails if no household is found`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        val households = listOf<UserHousehold>()
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)

        // When
        val result = useCase.addExpense(userMock, expense)

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

        whenever(expensesRepository.getAllExpensesFromHousehold(householdId))
            .thenReturn(Result.success(expenses))

        // When
        val result = useCase.getAllExpensesFromHousehold(householdId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expenses, result.getOrNull())
    }

    @Test
    fun `createHousehold creates a new household`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        val householdName = "New Household"
        val householdId = "household789"
        val household = UserHousehold(householdId, householdName)
        whenever(expensesRepository.createHousehold(any(), any()))
            .thenReturn(Result.success(household))

        // When
        val result = useCase.createHousehold(userId, householdName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(household, result.getOrNull())
        verify(expensesRepository).createHousehold(userId, householdName)
    }
}
