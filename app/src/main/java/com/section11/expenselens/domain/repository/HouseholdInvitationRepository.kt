package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserHousehold

interface HouseholdInvitationRepository {

    suspend fun postInvitationsToUser(
        inviterId: String,
        inviteeEmail: String,
        household: UserHousehold
    ) : Result<Unit>

    suspend fun getPendingInvitations(userId: String): Result<List<HouseholdInvite>>

    suspend fun acceptHouseholdInvite(userId: String, householdId: String, householdName: String): Result<Unit>

    suspend fun deleteHouseholdInvite(userId: String, householdId: String): Result<Unit>
}
