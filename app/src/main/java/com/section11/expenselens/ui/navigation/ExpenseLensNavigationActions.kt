package com.section11.expenselens.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

class ExpenseLensNavigationActions(private val navController: NavController) {

    fun navigateHome() = navController.navigate(HOME_ROUTE)

    fun navigateToCameraScreen() {
        navController.navigate(CAMERA_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    companion object {
        const val NAV_GRAPH_ROUTE = "expenseLensNavGraph"
        const val HOME_ROUTE = "home"
        const val CAMERA_ROUTE = "camera"
    }
}
