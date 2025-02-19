package com.section11.expenselens.domain.usecase

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.ExpensesRepository
import com.section11.expenselens.domain.repository.UserHouseholdsRepository
import javax.inject.Inject

class StoreExpenseUseCase @Inject constructor(
    private val expensesRepository: ExpensesRepository,
    private val userHouseholdsRepository: UserHouseholdsRepository
) {

    /**
     * Returns TO THE FIRST household ID found in expensesRepository.getCurrentHouseholdIds.
     * For now only 1 household will be allowed by user, but in the future we might need to add logic
     * if we want more than 1 household per user. The data will already support it, but not our logic
     */
    suspend fun getCurrentHousehold(userId: String): UserHousehold? {
        return userHouseholdsRepository.getUserHouseholds(userId).ifEmpty { null }?.first()
    }

    suspend fun createHousehold(
        userId: String,
        houseHoldName: String
    ): Result<UserHousehold> {
        val householdCreationResult = expensesRepository.createHousehold(userId, houseHoldName)
        val household = householdCreationResult.getOrNull()
        return if (householdCreationResult.isSuccess && household != null) {
            val addHouseholdToUserResult = userHouseholdsRepository.addHouseholdToUser(userId, household)

            addHouseholdToUserResult.fold(
                onSuccess = { Result.success(household) },
                onFailure = {
                    expensesRepository.deleteHousehold(userId, household.id)
                    Result.failure(it)
                }
            )
        } else {
            householdCreationResult
        }
    }

    suspend fun addExpense(
        userData: UserData,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit> {
        val currentHousehold = getCurrentHousehold(userData.id)
        return if (currentHousehold == null) {
            return Result.failure(HouseholdNotFoundException())
        } else {
            expensesRepository.addExpenseToHousehold(userData, currentHousehold.id, expense)
        }
    }

    /**
     * TODO: FirestoreExpense shouldnt be used in domain layer being a data object
     */
    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<FirestoreExpense>> {
        return expensesRepository.getAllExpensesFromHousehold(householdId)
    }
}
