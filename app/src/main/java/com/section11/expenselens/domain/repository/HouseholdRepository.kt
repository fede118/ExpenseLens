package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import java.util.Date

interface HouseholdRepository {

    suspend fun createHousehold(userId: String, householdName: String): Result<UserHousehold>

    suspend fun deleteHousehold(userId: String, householdId: String): Result<Unit>

    suspend fun addExpenseToHousehold(
        userData: UserData,
        householdId: String,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit>

    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<Expense>>

    suspend fun getExpensesForTimePeriod(
        householdId: String,
        firstDayOfCurrentMonth: Date,
        lastDayOfCurrentMonth: Date
    ): Result<List<Expense>>
}
