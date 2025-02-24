package com.section11.expenselens.domain.models

import com.google.firebase.Timestamp

class HouseholdInvite(
    val householdId: String,
    val householdName: String,
    val inviterId: String,
    val timestamp: Timestamp? = null,
    val status: HouseholdInviteStatus
) {
    enum class HouseholdInviteStatus {
        Pending,
        Accepted,
        Rejected
    }
}

