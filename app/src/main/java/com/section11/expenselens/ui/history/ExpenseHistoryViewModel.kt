package com.section11.expenselens.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    private val householdUseCase: HouseholdUseCase,
    private val useCase: SignInUseCase,
    dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Expense>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            val user = useCase.getCurrentUser().getOrNull()
            if (user != null) {
                val household = householdUseCase.getCurrentHousehold(user.id)
                if (household != null) {
                    _uiState.value = household.expenses
                }
            }
        }
    }
}
