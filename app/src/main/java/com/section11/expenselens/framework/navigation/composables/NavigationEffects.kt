package com.section11.expenselens.framework.navigation.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.section11.expenselens.framework.di.NavManagerEntryPoint
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToCameraScreen
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensesHistory
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToHouseholdDetails
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToManualExpenseInput
import com.section11.expenselens.ui.navigation.ExpenseLensNavigationActions
import dagger.hilt.android.EntryPointAccessors

@Composable
fun NavigationEffects(navController: NavController) {
    val appContext = LocalContext.current.applicationContext
    val navActions = ExpenseLensNavigationActions(navController)

    val navManager = remember {
        EntryPointAccessors.fromApplication(
            context = appContext,
            entryPoint = NavManagerEntryPoint::class.java
        ).getNavigationManager()
    }

    LaunchedEffect(navManager.navigationEvent) {
        navManager.navigationEvent.collect { event ->
            when(event) {
                is NavigateHome -> navActions.navigateHome(event.shouldUpdateHome)
                is NavigateToCameraScreen -> navActions.navigateToCameraScreen()
                is NavigateToExpensePreview -> navActions.navigateToExpensePreview(
                    event.extractedText,
                    event.suggestedExpenseInformation
                )
                is NavigateToManualExpenseInput -> navActions.navigateToManualExpenseInput()
                is NavigateToExpensesHistory -> navActions.navigateToExpensesHistory()
                is NavigateToHouseholdDetails -> { navActions.navigateToHouseholdDetails() }
            }
        }
    }
}
