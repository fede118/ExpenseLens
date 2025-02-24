package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import javax.inject.Inject

class HouseholdInvitationUseCase @Inject constructor(
    private val householdInvitationRepository: HouseholdInvitationRepository
) {

    suspend fun inviteToHousehold(
        inviterId: String,
        inviteeEmail: String,
        household: UserHousehold
    ): Result<Unit> {
        return householdInvitationRepository.postInvitationsToUser(
            inviterId,
            inviteeEmail,
            household
        )
    }

    suspend fun getPendingInvitations(userId: String): Result<List<HouseholdInvite>> {
        return householdInvitationRepository.getPendingInvitations(userId)
    }
}
