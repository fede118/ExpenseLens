package com.section11.expenselens.domain.repository

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation

interface ExpensesRepository {

    suspend fun getHousehold(householdName: String): String?

    suspend fun createHousehold(householdName: String, userId: String): Result<Pair<String, String>>

    suspend fun addExpenseToHousehold(
        userId: String,
        householdId: String,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit>

    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<FirestoreExpense>>
}
