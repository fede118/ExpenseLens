package com.section11.expenselens.ui.home.event

import android.content.Context
import com.section11.expenselens.ui.utils.UpstreamUiEvent

sealed class HomeUpstreamEvent : UpstreamUiEvent() {
    data object AddExpenseTapped : HomeUpstreamEvent()
    data object AddManualExpenseTapped : HomeUpstreamEvent()
    data class SignInTapped(val context: Context): HomeUpstreamEvent()
    data class CreateHouseholdTapped(
        val userId: String,
        val householdName: String
    ) : HomeUpstreamEvent()
    data class JoinHouseholdTapped(
        val userId: String,
        val userEmail: String
    ) : HomeUpstreamEvent()
    data class HouseholdInviteTap(
        val inviteId: String,
        val householdId: String,
        val householdName: String,
        val userId: String,
        val accepted: Boolean
    ) : HomeUpstreamEvent()
}

sealed class ProfileDialogEvents: HomeUpstreamEvent() {
    data object ToHouseholdDetailsTapped : HomeUpstreamEvent()
    data object ToExpensesHistoryTapped : HomeUpstreamEvent()
    data class AddUserToHouseholdTapped(
        val invitingUserId: String,
        val inviteeUserEmail: String
    ) : HomeUpstreamEvent()
    data object SignOutTapped : HomeUpstreamEvent()
}
