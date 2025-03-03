package com.section11.expenselens.domain.usecase

import com.google.firebase.Timestamp
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.Category.HOME
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
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

    @Before
    fun setup() {
        useCase = HouseholdUseCase(householdRepository, usersHouseholdRepository)
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
        whenever(householdRepository.addExpenseToHousehold(userMock, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpenseToCurrentHousehold(userMock, expense)

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
        whenever(householdRepository.addExpenseToHousehold(userMock, householdId, expense))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.addExpenseToCurrentHousehold(userMock, expense)

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
        val result = useCase.addExpenseToCurrentHousehold(userMock, expense)

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
