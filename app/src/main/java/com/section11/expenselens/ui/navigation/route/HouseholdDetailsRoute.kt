package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel
import com.section11.expenselens.ui.household.composables.HouseholdDetailsScreen
import com.section11.expenselens.ui.navigation.InterceptShowSnackBarDownStreamEvents

@Composable
fun HouseholdDetailsRoute(modifier: Modifier = Modifier) {
    val householdDetailsViewModel = hiltViewModel<HouseholdDetailsViewModel>()

    InterceptShowSnackBarDownStreamEvents(householdDetailsViewModel.uiEvent)

    HouseholdDetailsScreen(householdDetailsViewModel.uiState, householdDetailsViewModel.uiEvent, modifier) { event ->
        householdDetailsViewModel.onHouseholdDetailsUpstreamEvent(event)
    }
}
