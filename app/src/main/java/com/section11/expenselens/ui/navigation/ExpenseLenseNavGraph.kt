package com.section11.expenselens.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.navigation.composables.NavigationEffects
import com.section11.expenselens.framework.utils.getArg
import com.section11.expenselens.ui.camera.CameraPreviewViewModel
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.CAMERA_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSES_HISTORY_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_INFORMATION_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_REVIEW_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXTRACTED_TEXT_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_SHOULD_UPDATE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.NAV_GRAPH_ROUTE
import com.section11.expenselens.ui.navigation.route.CameraRoute
import com.section11.expenselens.ui.navigation.route.ExpenseHistoryRoute
import com.section11.expenselens.ui.navigation.route.ExpenseReviewRoute
import com.section11.expenselens.ui.navigation.route.HomeRoute
import com.section11.expenselens.ui.review.ExpenseReviewViewModel
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.theme.LocalSnackbarHostState
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

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

        composable(route = HOME_ROUTE) { navStackEntry ->
            val shouldUpdateHome = navStackEntry.arguments?.getBoolean(HOME_SHOULD_UPDATE)
            val homeViewModel = getHomeViewModelFromParentEntry(navController)
            InterceptShowSnackBarDownStreamEvents(homeViewModel.uiEvent)
            if (shouldUpdateHome == true) {
                homeViewModel.updateHomeInformation()
            }

            HomeRoute(
                homeUiStateFlow = homeViewModel.uiState,
                downstreamUiEvent = homeViewModel.uiEvent,
                dialogDownstreamUiEvent = homeViewModel.profileDialogUiEvent,
                onUpstreamEvent = homeViewModel::onUiEvent
            )

            // TODO: remove this for release
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

        composable(route = CAMERA_ROUTE) {  navStackEntry ->
            val cameraPreviewViewModel = hiltViewModel<CameraPreviewViewModel>(navStackEntry)
            CameraRoute(
                downstreamUiEvent = cameraPreviewViewModel.uiEvent,
                onUpstreamEvent = cameraPreviewViewModel::onUiEvent
            )
        }

        composable(route = EXPENSE_REVIEW_ROUTE) { navStackEntry ->
            val bundle = navStackEntry.arguments
            val expenseInfo = bundle?.getArg<SuggestedExpenseInformation>(EXPENSE_INFORMATION_KEY)
            val extractedTextFromImage = bundle?.getString(EXTRACTED_TEXT_KEY)
            val expenseReviewViewModel = hiltViewModel<ExpenseReviewViewModel>()

            InterceptShowSnackBarDownStreamEvents(expenseReviewViewModel.uiEvent)

            InitExpenseReviewViewModel(expenseReviewViewModel, expenseInfo, extractedTextFromImage)
            ExpenseReviewRoute(
                expenseReviewUiStateFlow = expenseReviewViewModel.uiState,
                downstreamUiEvent = expenseReviewViewModel.uiEvent,
                onUpstreamEvent = expenseReviewViewModel::onUpstreamEvent
            )
        }

        composable(route = EXPENSES_HISTORY_ROUTE) {
            val expenseHistoryViewModel = hiltViewModel<ExpenseHistoryViewModel>()

            ExpenseHistoryRoute(expenseHistoryViewModel.uiState)
        }
    }
}

@Composable
fun InterceptShowSnackBarDownStreamEvents(event: SharedFlow<DownstreamUiEvent>) {
    val uiEvent by event.collectAsState(null)
    val rememberCoroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    if (uiEvent is ShowSnackBar) {
        (uiEvent as? ShowSnackBar)?.run {
            rememberCoroutineScope.launch {
                snackbarHostState.showSnackbar(message, duration = duration)
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

@Composable
fun InitExpenseReviewViewModel(
    viewModel: ExpenseReviewViewModel,
    suggestedExpenseInformation: SuggestedExpenseInformation?,
    extractedText: String?
) {
    LaunchedEffect(viewModel) {
        viewModel.init(suggestedExpenseInformation, extractedText)
    }
}
