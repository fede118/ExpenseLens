package com.section11.expenselens.ui.common

import androidx.lifecycle.ViewModel
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel class for all ViewModels in the app.
 *
 * instead of having repeated all over the viewModels the uiState and event variables we have this
 * abstract viewModel to extend from
 */
@Suppress("VariableNaming")
abstract class AbstractViewModel: ViewModel() {

    protected val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    protected val _uiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val uiEvent: SharedFlow<DownstreamUiEvent> = _uiEvent

}
