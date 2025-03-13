package com.section11.expenselens.ui.history.event

import com.section11.expenselens.ui.utils.UpstreamUiEvent

sealed class ExpenseHistoryUpstreamEvent : UpstreamUiEvent() {
    data class OnExpenseHistoryItemDeleted(val expenseId: String) : ExpenseHistoryUpstreamEvent()
}
