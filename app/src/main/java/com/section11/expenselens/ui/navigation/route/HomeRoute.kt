package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.home.composables.HomeScreenContent
import com.section11.expenselens.ui.navigation.InterceptShowSnackBarDownStreamEvents
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeRoute(shouldUpdateHome: Boolean?) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    InterceptShowSnackBarDownStreamEvents(homeViewModel.uiEvent)
    if (shouldUpdateHome == true) {
        homeViewModel.updateHomeInformation()
    }

    HomeScreenContent(
        homeUiStateFlow = homeViewModel.uiState,
        downstreamUiEvent = homeViewModel.uiEvent,
        dialogDownstreamUiEvent = homeViewModel.profileDialogUiEvent,
        onEvent = homeViewModel::onUiEvent
    )
}

@DarkAndLightPreviews
@Composable
fun HomeScreenPreview() {
    val homeUiState = MutableStateFlow<UiState>(UiState.Idle)
    Preview {
        HomeScreenContent(
            homeUiStateFlow = homeUiState,
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow(),
        )
    }
}
