package com.section11.expenselens.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.navigation.composables.NavigationEffects
import com.section11.expenselens.framework.utils.getArg
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.CAMERA_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSES_HISTORY_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_INFORMATION_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXPENSE_REVIEW_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.EXTRACTED_TEXT_KEY
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOME_SHOULD_UPDATE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.HOUSEHOLD_DETAILS_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.MANUAL_EXPENSE_INPUT_ROUTE
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions.Companion.NAV_GRAPH_ROUTE
import com.section11.expenselens.ui.navigation.route.CameraRoute
import com.section11.expenselens.ui.navigation.route.ExpenseHistoryRoute
import com.section11.expenselens.ui.navigation.route.ExpenseReviewRoute
import com.section11.expenselens.ui.navigation.route.HomeRoute
import com.section11.expenselens.ui.navigation.route.HouseholdDetailsRoute
import com.section11.expenselens.ui.navigation.route.ManualExpenseInputRoute
import com.section11.expenselens.ui.review.ExpenseReviewViewModel
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
    NavigationEffects(navController)

    NavHost(
        route = NAV_GRAPH_ROUTE,
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(route = HOME_ROUTE) { navStackEntry ->
            val shouldUpdateHome = navStackEntry.arguments?.getBoolean(HOME_SHOULD_UPDATE)
            HomeRoute(shouldUpdateHome)
        }

        composable(route = CAMERA_ROUTE) {
            CameraRoute()
        }

        composable(route = EXPENSE_REVIEW_ROUTE) { navStackEntry ->
            val bundle = navStackEntry.arguments
            val expenseInfo = bundle?.getArg<SuggestedExpenseInformation>(EXPENSE_INFORMATION_KEY)
            val extractedTextFromImage = bundle?.getString(EXTRACTED_TEXT_KEY)
            ExpenseReviewRoute(expenseInfo, extractedTextFromImage)
        }

        composable(route = MANUAL_EXPENSE_INPUT_ROUTE) {
            ManualExpenseInputRoute()
        }

        composable(route = EXPENSES_HISTORY_ROUTE) {
            ExpenseHistoryRoute()
        }

        composable(route = HOUSEHOLD_DETAILS_ROUTE) {
            HouseholdDetailsRoute()
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
fun InitExpenseReviewViewModel(
    viewModel: ExpenseReviewViewModel,
    suggestedExpenseInformation: SuggestedExpenseInformation?,
    extractedText: String?
) {
    LaunchedEffect(viewModel) {
        viewModel.init(suggestedExpenseInformation, extractedText)
    }
}
