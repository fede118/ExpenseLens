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

    suspend fun handleHouseholdInviteResponse(
        inviteWasAccepted: Boolean,
        inviteId: String,
        userId: String,
        householdId: String,
        householdName: String
    ): Result<List<HouseholdInvite>> {
        if (inviteWasAccepted) {
            householdInvitationRepository.acceptHouseholdInvite(inviteId, userId, householdId, householdName)
        } else {
            householdInvitationRepository.deleteHouseholdInvite(inviteId, userId, householdId)
        }

        return getPendingInvitations(userId)
    }
}
