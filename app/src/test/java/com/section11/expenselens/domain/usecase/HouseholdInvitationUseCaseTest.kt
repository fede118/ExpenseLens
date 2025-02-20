package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
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
}
