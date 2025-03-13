package com.section11.expenselens.ui.history

import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.ui.common.AbstractViewModel
import com.section11.expenselens.ui.history.ExpenseHistoryViewModel.ExpenseHistoryUiState.ShowExpenseHistory
import com.section11.expenselens.ui.history.event.ExpenseHistoryUpstreamEvent
import com.section11.expenselens.ui.history.event.ExpenseHistoryUpstreamEvent.OnExpenseHistoryItemDeleted
import com.section11.expenselens.ui.history.mapper.ExpenseHistoryUiMapper
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    private val householdUseCase: HouseholdUseCase,
    private val signInUseCase: SignInUseCase,
    private val expenseHistoryUiMapper: ExpenseHistoryUiMapper,
    private val dispatcher: CoroutineDispatcher
) : AbstractViewModel() {

    private lateinit var householdId: String

    init {
        viewModelScope.launch(dispatcher) {
            val user = signInUseCase.getCurrentUser().getOrNull()
            if (user != null) {
                val household = householdUseCase.getCurrentHousehold(user.id)
                if (household != null) {
                    householdId = household.householdInfo.id
                    _uiState.value = ShowExpenseHistory(
                        expenseHistoryUiMapper.mapExpensesToUiItems(household.expenses)
                    )
                } else {
                    _uiState.value = UiState.Error("Household not found")
                }
            } else {
                _uiState.value = UiState.Error("User not found")
            }
        }
    }

    fun onUpstreamEvent(upstreamEvent: ExpenseHistoryUpstreamEvent) {
        when(upstreamEvent) {
            is OnExpenseHistoryItemDeleted -> {
                viewModelScope.launch(dispatcher) {
                    var tempDeletedExpense: ExpenseHistoryUiItem? = null
                    _uiState.update { currentState ->
                        (currentState as? ShowExpenseHistory)?.let { showExpenseHistoryState ->
                            tempDeletedExpense = showExpenseHistoryState.expenses.first {
                                it.expenseId == upstreamEvent.expenseId
                            }
                            currentState.copy(
                                expenses = expenseHistoryUiMapper.deleteExpenseFromList(
                                    showExpenseHistoryState.expenses,
                                    upstreamEvent.expenseId
                                )
                            )
                        } ?: currentState
                    }

                    householdUseCase.deleteExpenseFromHousehold(
                        householdId,
                        upstreamEvent.expenseId
                    ).onFailure {
                        _uiEvent.emit(ShowSnackBar("Couldn't delete expense"))
                        tempDeletedExpense?.let {
                            _uiState.update { currentState ->
                                (currentState as? ShowExpenseHistory)?.copy(
                                    expenses = currentState.expenses.plus(it)
                                ) ?: currentState
                            }
                        }
                    }
                }
            }
        }
    }

    sealed class ExpenseHistoryUiState : UiState() {
        data class ShowExpenseHistory(
            val expenses: List<ExpenseHistoryUiItem>
        ) : ExpenseHistoryUiState()
    }
}
