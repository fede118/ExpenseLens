package com.section11.expenselens.ui.home.event

import android.content.Context
import com.section11.expenselens.ui.utils.UpstreamUiEvent

sealed class HomeUpstreamEvent : UpstreamUiEvent() {
    data object AddExpenseTapped : HomeUpstreamEvent()
    data class SignInTapped(val context: Context): HomeUpstreamEvent() // Shouldn't be passing context around
    data class CreateHouseholdTapped(
        val userId: String,
        val householdName: String
    ) : HomeUpstreamEvent()
    data class HouseholdInviteTap(
        val householdId: String,
        val householdName: String,
        val userId: String,
        val accepted: Boolean
    ) : HomeUpstreamEvent()
}

sealed class ProfileDialogEvents: HomeUpstreamEvent() {
    data object ToExpensesHistoryTapped : HomeUpstreamEvent()
    data class AddUserToHouseholdTapped(
        val invitingUserId: String,
        val inviteeUserEmail: String
    ) : HomeUpstreamEvent()
    data object SignOutTapped : HomeUpstreamEvent()
}
