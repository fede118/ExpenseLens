package com.section11.expenselens.ui.home.dialog

import androidx.compose.ui.graphics.Color
import com.section11.expenselens.ui.utils.DownstreamUiEvent

sealed class DialogUiEvent : DownstreamUiEvent() {
    data class AddUserToHouseholdLoading(val isLoading: Boolean) : DialogUiEvent()
    data class HouseholdInviteResultEvent(
        val message: String,
        val textColor: Color
    ) : DialogUiEvent()
}
