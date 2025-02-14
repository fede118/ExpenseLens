package com.section11.expenselens.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.section11.expenselens.data.dto.FirestoreExpense
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
    private val firebaseAuth: FirebaseAuth,
    dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<FirestoreExpense>>(emptyList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val householdIdResult = storeExpensesUseCase.getCurrentHouseholdIdAndName(userId)
                val householdId = householdIdResult.getOrNull()?.first
                if (householdId != null) {
                    val expense = storeExpensesUseCase.getAllExpensesFromHousehold(householdId)

                    expense.getOrNull()?.let {
                        _uiState.value = it
                    }
                }
            }
        }
    }
}
