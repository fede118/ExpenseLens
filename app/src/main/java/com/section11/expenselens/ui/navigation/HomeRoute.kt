package com.section11.expenselens.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.Idle
import com.section11.expenselens.ui.home.composables.HomeScreenContent
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    homeUiState: StateFlow<HomeViewModel.HomeUiState>,
    onNavigationEvent: (NavigationEvent) -> Unit = {}
) {
    HomeScreenContent(modifier.fillMaxSize(), homeUiState) { event ->
        onNavigationEvent(event)
    }
}

@DarkAndLightPreviews
@Composable
fun HomeScreenPreview() {
    ExpenseLensTheme {
        Surface {
            HomeRoute(
                homeUiState = MutableStateFlow(Idle)
            )
        }
    }
}
