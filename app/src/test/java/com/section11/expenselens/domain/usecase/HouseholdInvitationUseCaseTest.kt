package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class HouseholdInvitationUseCaseTest {
    private val invitationsRepository: HouseholdInvitationRepository = mock()
    private val householdInvitationUseCase = HouseholdInvitationUseCase(invitationsRepository)

    @Test
    fun `inviteToHousehold should call the repository`() = runTest {
        // Given
        val inviterId = "inviterId"
        val inviteeEmail = "inviteeEmail"
        val household = UserHousehold("householdId", "householdName")

        // When
        householdInvitationUseCase.inviteToHousehold(inviterId, inviteeEmail, household)

        // Then
        verify(invitationsRepository).postInvitationsToUser(inviterId, inviteeEmail, household)
    }

    @Test
    fun `getPendingInvitations should call the repository`() = runTest {
        // Given
        val userId = "userId"

        // When
        householdInvitationUseCase.getPendingInvitations(userId)

        // Then
        verify(invitationsRepository).getPendingInvitations(userId)
    }

    @Test
    fun `handleHouseholdInviteResponse is accepted should call the repository`() = runTest {
        // Given
        val inviteWasAccepted = true
        val userId = "userId"
        val householdId = "householdId"
        val householdName = "householdName"

        // When
        householdInvitationUseCase.handleHouseholdInviteResponse(inviteWasAccepted, userId, householdId, householdName)

        // Then
        verify(invitationsRepository).acceptHouseholdInvite(userId, householdId, householdName)
        verify(invitationsRepository, never()).deleteHouseholdInvite(userId, householdId)
    }

    @Test
    fun `handleHouseholdInviteResponse is refused should call delete on the repository`() = runTest {
        // Given
        val inviteWasAccepted = false
        val userId = "userId"
        val householdId = "householdId"
        val householdName = "householdName"

        // When
        householdInvitationUseCase.handleHouseholdInviteResponse(
            inviteWasAccepted,
            userId,
            householdId,
            householdName
        )

        // Then
        verify(invitationsRepository, never()).acceptHouseholdInvite(
            userId,
            householdId,
            householdName
        )
        verify(invitationsRepository).deleteHouseholdInvite(userId, householdId)
    }
}
