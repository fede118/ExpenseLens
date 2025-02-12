package com.section11.expenselens.ui.review

import androidx.lifecycle.ViewModel
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UpstreamUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ExpenseReviewViewModel @Inject constructor(
    private val expenseReviewUiMapper: ExpenseReviewScreenUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun init(expenseInformation: ExpenseInformation?, extractedText: String?) {
        _uiState.value = ShowExpenseReview(
            expenseReviewUiMapper.mapExpenseInfoToUiModel(expenseInformation, extractedText)
        )
    }

    fun onUpstreamEvent(event: ExpenseReviewUpstreamEvent) {
        when(event) {
            is ExpenseSubmitted -> {
                // todo: submit expense
                Unit
            }
            is UserInputEvent -> updateExpenseReviewWithNewValues(event)
        }
    }

    private fun updateExpenseReviewWithNewValues(userInputEvent: UserInputEvent) {
        _uiState.update { state ->
            (state as? ShowExpenseReview)?.let { currentState ->
                currentState.copy(
                    expenseReviewUiModel = currentState.expenseReviewUiModel.copy(
                        reviewRows = currentState.expenseReviewUiModel.reviewRows.map { row ->
                            if (row.id == userInputEvent.id){
                                row.copy(value = userInputEvent.newValue)
                            } else {
                                row
                            }
                        }
                    )
                )
            } ?: state
        }
    }

    sealed class ExpenseReviewUiState : UiState() {
        data class ShowExpenseReview(val expenseReviewUiModel: ExpenseReviewUiModel) : ExpenseReviewUiState()
    }

    sealed class ExpenseReviewUpstreamEvent: UpstreamUiEvent() {
        data class UserInputEvent(val id: String, val newValue: String): ExpenseReviewUpstreamEvent()

        data class ExpenseSubmitted(
            val expenseReviewUiModel: ExpenseReviewUiModel
        ): ExpenseReviewUpstreamEvent()
    }
}
