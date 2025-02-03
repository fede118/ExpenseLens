package com.section11.expenselens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.CAMERA_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.NAV_GRAPH_ROUTE
import com.section11.expenselens.ui.navigation.NavigationEvent.AddExpenseTapped
import com.section11.expenselens.ui.navigation.NavigationEvent.TextExtractedFromImage

@Composable
fun ExpenseLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    val navigationActions = ExpenseLensNavigationActions(navController)

    NavHost(
        route = NAV_GRAPH_ROUTE,
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = HOME_ROUTE
        ) {
            val homeViewModel = getHomeViewModelFromParentEntry(navController)
            HomeRoute(homeUiState = homeViewModel.uiState) { navigationEvent ->
                handleNavigationEvent(navigationEvent, navigationActions)
            }
        }

        composable(
            route = CAMERA_ROUTE
        ) {
            val homeViewModel = getHomeViewModelFromParentEntry(navController)
            CameraRoute { navigationEvent ->
                if (navigationEvent is TextExtractedFromImage) {
                    // todo this is just POC this shouldnt be passed like this
                    homeViewModel.onTextExtractedFromImage(navigationEvent.extractedText)
                }
                handleNavigationEvent(navigationEvent, navigationActions)
            }
        }
    }
}

@Composable
fun getHomeViewModelFromParentEntry(navController: NavController): HomeViewModel {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(NAV_GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}

private fun handleNavigationEvent(
    navigationEvent: NavigationEvent,
    navigationActions: ExpenseLensNavigationActions
) {
    when(navigationEvent) {
        is AddExpenseTapped -> navigationActions.navigateToCameraScreen()
        is TextExtractedFromImage -> navigationActions.navigateHome()
    }
}
