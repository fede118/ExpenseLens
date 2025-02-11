package com.section11.expenselens.ui.navigation.route

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.section11.expenselens.ui.home.composables.HomeScreenContent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
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
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    HomeScreenContent(modifier.fillMaxSize(), homeUiState, downstreamUiEvent) { event ->
        onEvent(event)
    }
}

@DarkAndLightPreviews
@Composable
fun HomeScreenPreview() {
    val homeUiState = MutableStateFlow<UiState>(UiState.Idle)
    ExpenseLensTheme {
        Surface {
            HomeRoute(
                homeUiState = homeUiState,
                downstreamUiEvent = MutableSharedFlow(),
            ) {}
        }
    }
}
