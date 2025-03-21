package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.home.composables.HomeScreenContent
import com.section11.expenselens.ui.navigation.InterceptShowSnackBarDownStreamEvents
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
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
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        HomeScreenContent(
            homeUiStateFlow = MutableStateFlow(fakeRepo.getUserSignedInState(true)),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow(),
        )
    }
}

@DarkAndLightPreviews
@Composable
fun HomeScreenEmptyPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        HomeScreenContent(
            homeUiStateFlow = MutableStateFlow(fakeRepo.getUserSignedInState(false)),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow(),
        )
    }
}
