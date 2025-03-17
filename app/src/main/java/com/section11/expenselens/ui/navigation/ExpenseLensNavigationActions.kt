package com.section11.expenselens.ui.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.section11.expenselens.domain.models.SuggestedExpenseInformation

class ExpenseLensNavigationActions(private val navController: NavController) {

    fun navigateHome(shouldUpdateInformation: Boolean) = navController.navigate(
        route = HOME_ROUTE,
        args = bundleOf(HOME_SHOULD_UPDATE to shouldUpdateInformation),
        navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, true)
            .build()
    )

    fun navigateToCameraScreen() = navController.navigate(CAMERA_ROUTE)

    fun navigateToExpensesHistory() = navController.navigate(EXPENSES_HISTORY_ROUTE)

    fun navigateToExpensePreview(extractedText: String, suggestedExpenseInformation: SuggestedExpenseInformation) {
        navController.navigate(
            route = EXPENSE_REVIEW_ROUTE,
            args = bundleOf(
                EXPENSE_INFORMATION_KEY to suggestedExpenseInformation,
                EXTRACTED_TEXT_KEY to extractedText
            )
        )
    }

    fun navigateToManualExpenseInput() = navController.navigate(MANUAL_EXPENSE_INPUT_ROUTE)

    fun navigateToHouseholdDetails() = navController.navigate(HOUSEHOLD_DETAILS_ROUTE)


    private fun NavController.navigate(
        route: String,
        args: Bundle = bundleOf(),
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ) {
        val nodeId = graph.findNode(route = route)?.id
        if (nodeId != null) {
            navigate(nodeId, args, navOptions, navigatorExtras)
        }
    }

    companion object {
        const val NAV_GRAPH_ROUTE = "expenseLensNavGraph"
        const val HOME_ROUTE = "home"
        const val HOME_SHOULD_UPDATE = "shouldUpdateInformation"
        const val CAMERA_ROUTE = "camera"
        const val EXPENSE_INFORMATION_KEY = "expenseInformation"
        const val EXTRACTED_TEXT_KEY = "extractedText"
        const val EXPENSE_REVIEW_ROUTE = "expenseReview"
        const val MANUAL_EXPENSE_INPUT_ROUTE = "manualExpenseInput"
        const val EXPENSES_HISTORY_ROUTE = "expensesHistory"
        const val HOUSEHOLD_DETAILS_ROUTE = "householdDetails"
    }
}
