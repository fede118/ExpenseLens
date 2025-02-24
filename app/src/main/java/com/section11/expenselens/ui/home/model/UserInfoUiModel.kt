package com.section11.expenselens.ui.home.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoUiModel(
    val id: String,
    val displayName: String?,
    val profilePic: String?,
    val pendingInvites: List<PendingInvitesUiModel>
): Parcelable

@Parcelize
data class PendingInvitesUiModel(
    val id: String,
    val householdName: String,
    val timestamp: Timestamp?,
    val status: InviteStatusUiModel
) : Parcelable

@Parcelize
enum class InviteStatusUiModel : Parcelable {
    Pending,
    Accepted,
    Rejected
}
