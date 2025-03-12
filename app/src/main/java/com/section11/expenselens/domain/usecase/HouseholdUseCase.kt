package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.firstDayOfCurrentMonth
import com.section11.expenselens.domain.lastDayOfCurrentMonth
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import javax.inject.Inject

class HouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val usersCollectionRepository: UsersCollectionRepository,
    private val userSessionRepository: UserSessionRepository
) {

    /**
     * Returns TO THE FIRST household ID found in expensesRepository.getCurrentHouseholdIds.
     * For now only 1 household will be allowed by user, but in the future we might need to add logic
     * if we want more than 1 household per user. The data will already support it, but not our logic
     *
     * It also fetches the expenses for the current month and returns both in [HouseholdExpenses]
     */
    suspend fun getCurrentHousehold(userId: String): HouseholdExpenses? {
        val household = usersCollectionRepository.getUserHouseholds(userId).ifEmpty { null }?.first()
        return household?.let {
            val thisMonthExpenses = householdRepository.getExpensesForTimePeriod(
                it.id,
                firstDayOfCurrentMonth,
                lastDayOfCurrentMonth
            ).getOrElse { emptyList() }
            HouseholdExpenses(it, thisMonthExpenses)
        }
    }

    suspend fun createHousehold(
        userId: String,
        houseHoldName: String
    ): Result<UserHousehold> {
        val householdCreationResult = householdRepository.createHousehold(userId, houseHoldName)
        return householdCreationResult.fold(
            onSuccess = { household ->
                val addHouseholdToUserResult = usersCollectionRepository.addHouseholdToUser(
                    userId,
                    household
                )

                addHouseholdToUserResult.fold(
                    onSuccess = {
                        userSessionRepository.updateCurrentHouseholdId(household.id)
                        Result.success(household)
                    },
                    onFailure = {
                        householdRepository.deleteHousehold(userId, household.id)
                        Result.failure(it)
                    }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun addExpenseToCurrentHousehold(
        userData: UserData,
        expense: ConsolidatedExpenseInformation
    ): Result<Unit> {
        return with(userData) {
            if (currentHouseholdId == null) {
                return Result.failure(HouseholdNotFoundException())
            } else {
                householdRepository.addExpenseToHousehold(
                    userData,
                    currentHouseholdId,
                    expense
                )
            }
        }
    }

    suspend fun deleteExpenseFromHousehold(householdId: String, expenseId: String): Result<Unit> {
        return householdRepository.deleteExpenseFromHousehold(householdId, expenseId)
    }

    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<Expense>> {
        return householdRepository.getAllExpensesFromHousehold(householdId)
    }
}
