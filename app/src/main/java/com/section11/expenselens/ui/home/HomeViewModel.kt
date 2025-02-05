package com.section11.expenselens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.ui.utils.UiEvent
import com.section11.expenselens.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onUiEvent(homeEvent: HomeEvent) {
        viewModelScope.launch(dispatcher) {
            when(homeEvent) {
                is HomeEvent.AddExpenseTapped -> {
                    navigationManager.navigate(NavigationEvent.NavigateToCameraScreen)
                }
            }
        }
    }

    sealed class HomeUiState : UiState()

    // TODO decide where to put this. Events on Camera preview are not inside of the viewModel
    // chose one structure and commit. To me it makes sense that the viewModel doesn't have the events
    sealed class HomeEvent : UiEvent() {
        data object AddExpenseTapped : HomeEvent()
    }
}
