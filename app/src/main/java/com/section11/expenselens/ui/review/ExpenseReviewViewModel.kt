package com.section11.expenselens.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.ui.common.UiConstants.SNACKBAR_DELAY
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.validator.ExpenseValidator
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UpstreamUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val AUTHENTICATION_ERROR = "Authentication Error occurred, please sign in again"
private const val SUBMIT_EXPENSE_SUCCESS = "Expense added successfully"
private const val SUBMIT_EXPENSE_ERROR = "Couldn't submit expense, try again later"

@HiltViewModel
class ExpenseReviewViewModel @Inject constructor(
    private val expenseReviewUiMapper: ExpenseReviewScreenUiMapper,
    private val householdUseCase: HouseholdUseCase,
    private val signInUseCase: SignInUseCase,
    private val navigationManager: NavigationManager,
    private val expenseValidator: ExpenseValidator,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val uiEvent: SharedFlow<DownstreamUiEvent> = _uiEvent

    fun init(suggestedExpenseInformation: SuggestedExpenseInformation?, extractedText: String?) {
        if (suggestedExpenseInformation != null) {
            _uiState.value = ShowExpenseReview(
                expenseReviewUiMapper.mapExpenseInfoToUiModel(
                    suggestedExpenseInformation,
                    extractedText
                )
            )
        } else {
            val (errorMessage, uiModel) = expenseReviewUiMapper.getNoExpenseFoundMessageAndUiModel()
            viewModelScope.launch(dispatcher) {
                _uiEvent.emit(ShowSnackBar(errorMessage))
            }
            _uiState.value = ShowExpenseReview(uiModel)
        }
    }

    fun initEmpty() {
        _uiState.value = ShowExpenseReview(
            expenseReviewUiMapper.getEmptyExpenseReviewUiModel()
        )
    }

    fun onUpstreamEvent(event: ExpenseReviewUpstreamEvent) {
        when(event) {
            is ExpenseSubmitted -> {
                viewModelScope.launch(dispatcher) {
                    _uiEvent.emit(Loading(true))
                    val user = signInUseCase.getCurrentUser().getOrNull()
                    if (user != null) {
                        val consolidateExpenseResult = expenseValidator.validateExpense(event)
                        consolidateExpenseResult.onFailure {
                            _uiEvent.emit(ShowSnackBar(getFieldValidationErrorMessage(it)))
                        }
                        consolidateExpenseResult.onSuccess { expense ->
                            val result = householdUseCase.addExpenseToCurrentHousehold(user, expense)
                            handleExpenseSubmission(result)
                        }
                    } else {
                        _uiEvent.emit(ShowSnackBar(AUTHENTICATION_ERROR))
                        navigationManager.navigate(NavigateHome())
                    }
                    _uiEvent.emit(Loading(false))
                }
            }
            is UserInputEvent -> updateExpenseReviewWithNewValues(event)
        }
    }

    private fun getFieldValidationErrorMessage(exception: Throwable): String {
        return expenseReviewUiMapper.getErrorMessageFromExpenseValidationException(exception)
    }

    private suspend fun handleExpenseSubmission(result: Result<Unit>) {
        if (result.isSuccess) {
            _uiEvent.emit(ShowSnackBar(SUBMIT_EXPENSE_SUCCESS))
            delay(SNACKBAR_DELAY)
            navigationManager.navigate(NavigateHome(shouldUpdateHome = true))
        } else {
            _uiEvent.emit(ShowSnackBar(SUBMIT_EXPENSE_ERROR))
        }
    }

    private fun updateExpenseReviewWithNewValues(userInputEvent: UserInputEvent) {
        _uiState.update { state ->
            (state as? ShowExpenseReview)?.let { currentState ->
                updateRowValue(currentState, userInputEvent)
            } ?: state
        }
    }

    private fun updateRowValue(
        currentState: ShowExpenseReview,
        userInputEvent: UserInputEvent
    ): ShowExpenseReview {
        return currentState.copy(
            expenseReviewUiModel = currentState.expenseReviewUiModel.copy(
                reviewRows = currentState.expenseReviewUiModel.reviewRows.map { row ->
                    if (row.section == userInputEvent.section){
                        row.copy(value = userInputEvent.newValue)
                    } else {
                        row
                    }
                }
            )
        )
    }

    sealed class ExpenseReviewUiState : UiState() {
        data class ShowExpenseReview(val expenseReviewUiModel: ExpenseReviewUiModel) : ExpenseReviewUiState()
    }

    sealed class ExpenseReviewUpstreamEvent: UpstreamUiEvent() {
        data class UserInputEvent(
            val section: ExpenseReviewSections,
            val newValue: String
        ): ExpenseReviewUpstreamEvent()

        data class ExpenseSubmitted(
            val expenseReviewUiModel: ExpenseReviewUiModel
        ): ExpenseReviewUpstreamEvent()
    }
}
