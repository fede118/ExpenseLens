package com.section11.expenselens.ui.home.event

import android.content.Context
import com.section11.expenselens.ui.utils.UpstreamUiEvent

sealed class HomeUpstreamEvent : UpstreamUiEvent() {
    data object AddExpenseTapped : HomeUpstreamEvent()
    // Shouldn't be passing context around
    data class SignInTapped(val context: Context): HomeUpstreamEvent()
    data object SignOutTapped : HomeUpstreamEvent()
    data object ToExpensesHistoryTapped : HomeUpstreamEvent()
}
