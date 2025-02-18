package com.section11.expenselens.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.domain.usecase.StoreExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    private val storeExpensesUseCase: StoreExpenseUseCase,
    private val useCase: GoogleSignInUseCase,
    dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<FirestoreExpense>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            val user = useCase.getCurrentUser().getOrNull()
            if (user != null) {
                val household = storeExpensesUseCase.getCurrentHousehold(user.id)
                if (household != null) {
                    val expenses = storeExpensesUseCase.getAllExpensesFromHousehold(household.id)

                    expenses.getOrNull()?.let { expensesList ->
                        _uiState.value = expensesList.map {
                            if (it.userDisplayName.isNullOrEmpty()) {
                                it.copy(userDisplayName = null)
                            } else {
                                it
                            }
                        }
                    }
                }
            }
        }
    }
}
