package com.section11.expenselens.data.dto

data class FirestoreHouseholdInvitation(
    val id: String,
    val name: String,
    val inviterId: String,
    val timestamp: Long?,
    val status: HouseholdInviteStatus
) {
    enum class HouseholdInviteStatus {
        Pending,
        Accepted,
        Rejected
    }
}
