package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import com.section11.expenselens.ui.home.composables.HomeScreenContent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeRoute(
    homeUiStateFlow: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onUpstreamEvent: (HomeUpstreamEvent) -> Unit = {}
) {
    HomeScreenContent(homeUiStateFlow, downstreamUiEvent, dialogDownstreamUiEvent, onUpstreamEvent)
}

@DarkAndLightPreviews
@Composable
fun HomeScreenPreview() {
    val homeUiState = MutableStateFlow<UiState>(UiState.Idle)
    Preview {
        HomeRoute(
            homeUiStateFlow = homeUiState,
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow(),
        )
    }
}
