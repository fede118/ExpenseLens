package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.UserHousehold

interface HouseholdInvitationRepository {

    suspend fun postInvitationsToUser(
        inviterId: String,
        inviteeEmail: String,
        household: UserHousehold
    ) : Result<Unit>
}
