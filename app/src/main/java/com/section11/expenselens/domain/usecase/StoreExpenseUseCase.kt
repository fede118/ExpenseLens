package com.section11.expenselens.domain.usecase

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.repository.ExpensesRepository
import javax.inject.Inject

// TODO this should input from the user
private const val TEST_HOUSEHOLD_NAME = "Test Household"

class StoreExpenseUseCase @Inject constructor(
    private val expensesRepository: ExpensesRepository
) {

    suspend fun getCurrentHouseholdIdAndName(userId: String): Result<Pair<String, String>> {
        val currentHouseholdId = expensesRepository.getHousehold(TEST_HOUSEHOLD_NAME)

        return if (currentHouseholdId != null) {
            Result.success(currentHouseholdId to TEST_HOUSEHOLD_NAME)
        } else {
            createHousehold(userId)
        }
    }

    private suspend fun createHousehold(userId: String): Result<Pair<String, String>> {
        return expensesRepository.createHousehold(TEST_HOUSEHOLD_NAME, userId)
    }

    suspend fun addExpense(
        userId: String,
        expense: ConsolidatedExpenseInformation,
        householdId: String? = null
    ): Result<Unit> {
        var currentHouseholdId = householdId
        if (currentHouseholdId == null) {
            currentHouseholdId = expensesRepository.getHousehold(TEST_HOUSEHOLD_NAME)
            currentHouseholdId ?: return Result.failure(HouseholdNotFoundException())
        }
        return expensesRepository.addExpenseToHousehold(userId, currentHouseholdId, expense)
    }

    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<FirestoreExpense>> {
        return expensesRepository.getAllExpensesFromHousehold(householdId)
    }
}
