package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.HouseholdDetails
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class HouseholdDetailsUseCaseTest {

    private val householdRepository: HouseholdRepository = mock()
    private val usersCollectionRepository: UsersCollectionRepository = mock()
    private val userSessionRepository: UserSessionRepository = mock()

    private lateinit var useCase: HouseholdDetailsUseCase

    @Before
    fun setup() {
        useCase = HouseholdDetailsUseCase(
            householdRepository,
            usersCollectionRepository,
            userSessionRepository
        )
    }

    @Test
    fun `getCurrentHouseholdDetails returns household details`() = runTest {
        // Given
        val householdId = "household123"
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")
        val householdDetails = HouseholdDetails(householdId, householdName, usersIds)
        val userEmails = listOf("email", "email2")
        val userDataMock: UserData = mock()
        whenever(userDataMock.currentHouseholdId).thenReturn(householdId)
        whenever(userSessionRepository.getUser()).thenReturn(userDataMock)
        whenever(householdRepository.getHouseholdDetails(householdId))
            .thenReturn(Result.success(householdDetails))
        whenever(usersCollectionRepository.getListOfUserEmails(usersIds)).thenReturn(userEmails)

        // When
        val result = useCase.getCurrentHouseholdDetails()

        // Then
        assert(result.isSuccess)
        assert(result.getOrNull() is HouseholdDetailsWithUserEmails)
        val householdDetailsWithUserEmails = result.getOrNull() as HouseholdDetailsWithUserEmails
        assert(householdDetailsWithUserEmails.id == householdId)
        assert(householdDetailsWithUserEmails.name == householdName)
        householdDetailsWithUserEmails.usersEmails.forEach {
            assert(userEmails.contains(it))
        }
    }

    @Test
    fun `getCurrentHouseholdDetails returns failure when user has no household`() = runTest {
        // Given
        val userDataMock: UserData = mock()
        whenever(userDataMock.currentHouseholdId).thenReturn(null)
        whenever(userSessionRepository.getUser()).thenReturn(userDataMock)

        // When
        val result = useCase.getCurrentHouseholdDetails()

        // Then
        assert(result.isFailure)
    }

    @Test
    fun `getCurrentHouseholdDetails returns failure when household details are not found`() = runTest {
        // Given
        val householdId = "household123"
        val userDataMock: UserData = mock()
        whenever(userDataMock.currentHouseholdId).thenReturn(householdId)
        whenever(userSessionRepository.getUser()).thenReturn(userDataMock)
        whenever(householdRepository.getHouseholdDetails(householdId))
            .thenReturn(Result.failure(Exception()))

        // When
        val result = useCase.getCurrentHouseholdDetails()

        // Then
        assert(result.isFailure)
    }
}
