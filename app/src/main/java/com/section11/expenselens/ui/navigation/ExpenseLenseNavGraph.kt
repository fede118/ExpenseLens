package com.section11.expenselens.ui.navigation

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.framework.navigation.composables.NavigationEffects
import com.section11.expenselens.ui.camera.CameraPreviewViewModel
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.CAMERA_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_INFORMATION_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_REVIEW_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXTRACTED_TEXT_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.NAV_GRAPH_ROUTE
import com.section11.expenselens.ui.navigation.route.CameraRoute
import com.section11.expenselens.ui.navigation.route.ExpenseReviewRoute
import com.section11.expenselens.ui.navigation.route.HomeRoute

@Composable
fun ExpenseLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    NavigationEffects(navController)

    NavHost(
        route = NAV_GRAPH_ROUTE,
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = HOME_ROUTE) {
            val homeViewModel = getHomeViewModelFromParentEntry(navController)
            HomeRoute(
                homeUiState = homeViewModel.uiState,
                downstreamUiEvent = homeViewModel.uiEvent,
                onEvent = homeViewModel::onUiEvent
            )
        }

        composable(route =  CAMERA_ROUTE) {  navStackEntry ->
            val cameraPreviewViewModel = hiltViewModel<CameraPreviewViewModel>(navStackEntry)
            CameraRoute(
                downstreamUiEvent = cameraPreviewViewModel.uiEvent,
                onEvent = cameraPreviewViewModel::onUiEvent
            )
        }

        composable(route = EXPENSE_REVIEW_ROUTE) { navStackEntry ->
            val args = navStackEntry.arguments
            val expenseInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args?.getParcelable(EXPENSE_INFORMATION_KEY, ExpenseInformation::class.java)
            } else {
                @Suppress("DEPRECATION")
                args?.getParcelable(EXPENSE_INFORMATION_KEY) as? ExpenseInformation
            }
            val extractedTextFromImage = args?.getString(EXTRACTED_TEXT_KEY) ?: "No extracted text"

            ExpenseReviewRoute(expenseInfo, extractedTextFromImage)
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
