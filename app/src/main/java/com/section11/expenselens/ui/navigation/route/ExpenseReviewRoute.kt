package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent
import com.section11.expenselens.ui.review.composables.ExpenseReviewScreen
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ExpenseReviewRoute(
    expenseReviewUiStateFlow: StateFlow<UiState>,
    onEvent: (ExpenseReviewUpstreamEvent) -> Unit
) {
    ExpenseReviewScreen(expenseReviewUiStateFlow = expenseReviewUiStateFlow) { event ->
        onEvent(event)
    }
}

@DarkAndLightPreviews
@Composable
fun ExpenseReviewRoutePreview(modifier: Modifier = Modifier) {
    val fakeRepository = FakeRepositoryForPreviews(LocalContext.current)
    val expenseInfo = fakeRepository.getExpenseReviewUiModel()
    val uiState = MutableStateFlow<UiState>(ShowExpenseReview(expenseInfo))
    Preview {
        ExpenseReviewScreen(modifier = modifier, expenseReviewUiStateFlow = uiState)
    }
}
