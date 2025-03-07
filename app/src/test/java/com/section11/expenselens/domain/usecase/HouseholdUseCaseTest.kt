package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

private const val TEST_HOUSEHOLD_NAME = "Test Household"

@ExperimentalCoroutinesApi
class HouseholdUseCaseTest {

    private lateinit var useCase: HouseholdUseCase
    private val householdRepository: HouseholdRepository = mock()
    private val usersHouseholdRepository: UsersCollectionRepository = mock()
    private val userSessionRepository: UserSessionRepository = mock()

    @Before
    fun setup() {
        useCase = HouseholdUseCase(
            householdRepository,
            usersHouseholdRepository,
            userSessionRepository
        )
    }

    @Test
    fun `getCurrentHousehold returns existing household ID`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val households = listOf(UserHousehold(householdId, TEST_HOUSEHOLD_NAME))
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)
        whenever(householdRepository.getExpensesForTimePeriod(any(), any(), any()))
            .thenReturn(Result.success(emptyList()))

        // When
        val result = useCase.getCurrentHousehold(userId)

        // Then
        assertEquals(householdId, result?.householdInfo?.id)
    }

    @Test
    fun `getCurrentHousehold also returns expenseList`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household456"
        val households = listOf(UserHousehold(householdId, TEST_HOUSEHOLD_NAME))
        whenever(usersHouseholdRepository.getUserHouseholds(any())).thenReturn(households)
        val expenses = listOf(
            Expense(
                total = 100.0,
                category = "Home",
                date = Date(),
                userId = "user123",
                note = "Dinner",
                userDisplayName = "Test User",
                distributedExpense = emptyMap()
            )
        )
        whenever(householdRepository.getExpensesForTimePeriod(any(), any(), any()))
            .thenReturn(Result.success(expenses))

        // When
        val result = useCase.getCurrentHousehold(userId)

        // Then
        assertEquals(expenses, result?.expenses)
    }

    @Test
    fun `addExpense successfully adds expense when household ID is provided`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        whenever(userMock.currentHouseholdId).thenReturn("someId")
        val householdId = "household456"
        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )
        whenever(householdRepository.addExpenseToHousehold(userMock, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpenseToCurrentHousehold(userMock, expense)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `addExpense retrieves fails if no householdId`() = runTest {
        // Given
        val userId = "user123"
        val userMock: UserData = mock()
        whenever(userMock.id).thenReturn(userId)
        whenever(userMock.displayName).thenReturn("Test User")
        whenever(userMock.currentHouseholdId).thenReturn(null)

        val expense = ConsolidatedExpenseInformation(
            total = 100.0,
            category = HOME,
            date = Date(),
            note = "Dinner",
            distributedExpense = mapOf(userId to 50.0)
        )

        // When
        val result = useCase.addExpenseToCurrentHousehold(userMock, expense)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `getAllExpensesFromHousehold returns list of expenses`() = runTest {
        // Given
        val householdId = "household456"
        val expenses = listOf(
            Expense(
                total = 100.0,
                category = "Home",
                date = Date(),
                userId = "user123",
                note = "Dinner",
                userDisplayName = "Test User",
                distributedExpense = emptyMap()
            )
        )

        whenever(householdRepository.getAllExpensesFromHousehold(householdId))
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
        whenever(householdRepository.createHousehold(any(), any()))
            .thenReturn(Result.success(household))

        // When
        val result = useCase.createHousehold(userId, householdName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(household, result.getOrNull())
        verify(householdRepository).createHousehold(userId, householdName)
    }

    @Test
    fun `when create household fails then should return failure`() = runTest {
        // Given
        whenever(householdRepository.createHousehold(any(), any()))
            .thenReturn(Result.failure(Exception()))

        // When
        val result = useCase.createHousehold("userId", "householdName")

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `when create household is success then should also add user to household`() = runTest {
        // Given
        val userId = "userId"
        val householdName = "householdName"
        val householdId = "householdId"
        val userHousehold: UserHousehold = mock()
        whenever(userHousehold.name).thenReturn(householdName)
        whenever(userHousehold.id).thenReturn(householdId)
        whenever(householdRepository.createHousehold(any(), any()))
            .thenReturn(Result.success(userHousehold))
        whenever(usersHouseholdRepository.addHouseholdToUser(any(), any()))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.createHousehold(userId, householdName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(userHousehold, result.getOrNull())
    }

    @Test
    fun `when create household is success but add user fails then delete household`() = runTest {
        // Given
        val userId = "userId"
        val householdName = "householdName"
        val householdId = "householdId"
        val userHousehold: UserHousehold = mock()
        whenever(userHousehold.name).thenReturn(householdName)
        whenever(userHousehold.id).thenReturn(householdId)
        whenever(householdRepository.createHousehold(any(), any()))
            .thenReturn(Result.success(userHousehold))
        whenever(usersHouseholdRepository.addHouseholdToUser(any(), any()))
            .thenReturn(Result.failure(Exception()))

        // When
        val result = useCase.createHousehold(userId, householdName)

        // Then
        assertTrue(result.isFailure)
        verify(householdRepository).deleteHousehold(userId, householdId)
    }
}
