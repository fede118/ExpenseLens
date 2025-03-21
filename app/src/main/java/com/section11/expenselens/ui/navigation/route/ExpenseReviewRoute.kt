package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.navigation.InitExpenseReviewViewModel
import com.section11.expenselens.ui.navigation.InterceptShowSnackBarDownStreamEvents
import com.section11.expenselens.ui.review.ExpenseReviewViewModel
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.composables.ExpenseReviewScreen
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ExpenseReviewRoute(expenseInfo: SuggestedExpenseInformation?, extractedTextFromImage: String?) {
    val expenseReviewViewModel = hiltViewModel<ExpenseReviewViewModel>()

    InterceptShowSnackBarDownStreamEvents(expenseReviewViewModel.uiEvent)

    InitExpenseReviewViewModel(expenseReviewViewModel, expenseInfo, extractedTextFromImage)

    ExpenseReviewScreen(
        expenseReviewUiStateFlow = expenseReviewViewModel.uiState,
        downstreamUiEvent = expenseReviewViewModel.uiEvent,
        onUpstreamUiEvent = expenseReviewViewModel::onUpstreamEvent
    )
}

@DarkAndLightPreviews
@Composable
fun ExpenseReviewRoutePreview(modifier: Modifier = Modifier) {
    val fakeRepository = FakeRepositoryForPreviews(LocalContext.current)
    val expenseInfo = fakeRepository.getExpenseReviewUiModel()
    val uiState = MutableStateFlow<UiState>(ShowExpenseReview(expenseInfo))
    Preview {
        ExpenseReviewScreen(
            modifier = modifier,
            expenseReviewUiStateFlow = uiState,
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}
