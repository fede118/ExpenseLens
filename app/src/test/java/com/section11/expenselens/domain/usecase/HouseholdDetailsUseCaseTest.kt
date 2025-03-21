package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.HouseholdDetails
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
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
        val currentUserId = "user123"
        val householdId = "household123"
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")
        val householdDetails = HouseholdDetails(householdId, householdName, usersIds)
        val userEmails = listOf("email", "email2")
        val userDataMock: UserData = mock()
        whenever(userDataMock.currentHouseholdId).thenReturn(householdId)
        whenever(userDataMock.id).thenReturn(currentUserId)
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
        assert(householdDetailsWithUserEmails.householdId == householdId)
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

    @Test
    fun `leaveHousehold removes user from household and updates user session`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household123"
        val userHouseholds = listOf(UserHousehold(householdId, "Test Household"))
        whenever(usersCollectionRepository.getUserHouseholds(userId)).thenReturn(userHouseholds)

        // When
        val result = useCase.leaveHousehold(userId, householdId)

        // Then
        assert(result.isSuccess)
        verify(userSessionRepository).updateCurrentHouseholdId(householdId)
        verify(usersCollectionRepository).removeHouseholdFromUser(userId, householdId)
        verify(householdRepository).removeUserFromHousehold(userId, householdId)
    }

    @Test
    fun `leaveHousehold when household is emptyList it updates currentHousehold to null`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household123"
        val userHouseholds = listOf<UserHousehold>()
        whenever(usersCollectionRepository.getUserHouseholds(userId)).thenReturn(userHouseholds)

        // When
        val result = useCase.leaveHousehold(userId, householdId)

        // Then
        assert(result.isSuccess)
        verify(userSessionRepository).updateCurrentHouseholdId(null)
        verify(usersCollectionRepository).removeHouseholdFromUser(userId, householdId)
        verify(householdRepository).removeUserFromHousehold(userId, householdId)
    }

    @Test
    fun `deleteHousehold removes household from user and deletes household`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household123"
        whenever(usersCollectionRepository.removeHouseholdFromUser(userId, householdId))
            .thenReturn(Result.success(Unit))
        whenever(householdRepository.deleteHousehold(userId, householdId))
            .thenReturn(Result.success(Unit))

        // When
        val result = useCase.deleteHousehold(userId, householdId)

        // Then
        assert(result.isSuccess)
        verify(usersCollectionRepository).removeHouseholdFromUser(userId, householdId)
        verify(householdRepository).deleteHousehold(userId, householdId)
    }

    @Test
    fun `deleteHousehold returns failure when removing household from user fails`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household123"
        whenever(usersCollectionRepository.removeHouseholdFromUser(userId, householdId))
            .thenReturn(Result.failure(Exception()))

        // When
        val result = useCase.deleteHousehold(userId, householdId)

        // Then
        assert(result.isFailure)
    }

    @Test
    fun `deleteHousehold returns failure when deleting household fails`() = runTest {
        // Given
        val userId = "user123"
        val householdId = "household123"
        whenever(usersCollectionRepository.removeHouseholdFromUser(userId, householdId))
            .thenReturn(Result.success(Unit))
        whenever(householdRepository.deleteHousehold(userId, householdId))
            .thenReturn(Result.failure(Exception()))

        // When
        val result = useCase.deleteHousehold(userId, householdId)

        // Then
        assert(result.isFailure)
    }
}
