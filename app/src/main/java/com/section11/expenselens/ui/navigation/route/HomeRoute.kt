package com.section11.expenselens.ui.navigation.route

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
    homeUiState: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onUpstreamEvent: (HomeUpstreamEvent) -> Unit
) {
    HomeScreenContent(modifier.fillMaxSize(), homeUiState, downstreamUiEvent) { event ->
        onUpstreamEvent(event)
    }
}

@DarkAndLightPreviews
@Composable
fun HomeScreenPreview() {
    val homeUiState = MutableStateFlow<UiState>(UiState.Idle)
    Preview {
        HomeRoute(
            homeUiState = homeUiState,
            downstreamUiEvent = MutableSharedFlow(),
        ) {}
    }
}
