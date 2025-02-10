package com.section11.expenselens.ui.home.event

import android.content.Context
import com.section11.expenselens.ui.utils.UiEvent

sealed class HomeUiEvent : UiEvent() {
    data object AddExpenseTapped : HomeUiEvent()
    // Shouldn't be passing context around
    data class SignInTapped(val context: Context): HomeUiEvent()
    data object SignOutTapped : HomeUiEvent()
}
