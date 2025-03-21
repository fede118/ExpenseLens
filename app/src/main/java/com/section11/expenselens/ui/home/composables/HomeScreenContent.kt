package com.section11.expenselens.ui.home.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.section11.expenselens.ui.common.HandleDownstreamEvents
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreenContent(
    homeUiStateFlow: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit = {}
) {
    val uiState by homeUiStateFlow.collectAsState()
    HandleDownstreamEvents(downstreamUiEvent, initialState = Loading(uiState == UiState.Idle))

    when (uiState) {
        is UserSignedIn -> SignedInUi(uiState as UserSignedIn, dialogDownstreamUiEvent, onEvent)
        is UserSignedOut -> LoggedOutUi(onEvent = onEvent)
    }
}
