package com.section11.expenselens.framework.navigation

import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.ui.utils.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface NavigationManager {

    val navigationEvent: SharedFlow<NavigationEvent>

    suspend fun navigate(event: NavigationEvent)

    sealed class NavigationEvent : UiEvent() {
        data object NavigateHome : NavigationEvent()
        data object NavigateToCameraScreen : NavigationEvent()
        data class NavigateToExpensePreview(
            val extractedText: String,
            val suggestedExpenseInformation: SuggestedExpenseInformation
        ) : NavigationEvent()
        data object NavigateToExpensesHistory : NavigationEvent()
    }
}

class NavigationManagerImpl : NavigationManager {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    override val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    override suspend fun navigate(event: NavigationEvent) {
        _navigationEvent.emit(event)
    }
}
