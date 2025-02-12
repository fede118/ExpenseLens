package com.section11.expenselens.ui.navigation

import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import com.section11.expenselens.ui.review.ExpenseReviewViewModel
import com.section11.expenselens.ui.theme.LocalDimens

@Composable
fun ExpenseLensNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    val context = LocalContext.current
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

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text("Just for development:", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(LocalDimens.current.mHalf))
                    Button(
                        onClick = { homeViewModel.dummyButtonForTesting(context) }
                    ) { Text("To ExpenseReview") }
                }

            }
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
            val expenseInfo = getExpenseInfo(args)
            val extractedTextFromImage = args?.getString(EXTRACTED_TEXT_KEY)
            val expenseReviewViewModel = hiltViewModel<ExpenseReviewViewModel>()

            InitExpenseReviewViewModel(expenseReviewViewModel, expenseInfo, extractedTextFromImage)
            ExpenseReviewRoute(
                expenseReviewUiStateFlow = expenseReviewViewModel.uiState,
                onEvent = expenseReviewViewModel::onUpstreamEvent
            )
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

private fun getExpenseInfo(args: Bundle?): ExpenseInformation? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        args?.getParcelable(EXPENSE_INFORMATION_KEY, ExpenseInformation::class.java)
    } else {
        @Suppress("DEPRECATION")
        args?.getParcelable(EXPENSE_INFORMATION_KEY) as? ExpenseInformation
    }
}

@Composable
fun InitExpenseReviewViewModel(
    viewModel: ExpenseReviewViewModel,
    expenseInformation: ExpenseInformation?,
    extractedText: String?
) {
    LaunchedEffect(viewModel) {
        viewModel.init(expenseInformation, extractedText)
    }
}
