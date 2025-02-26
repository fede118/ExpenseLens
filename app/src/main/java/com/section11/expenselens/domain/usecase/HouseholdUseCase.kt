package com.section11.expenselens.domain.usecase

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import javax.inject.Inject

class HouseholdUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val usersCollectionRepository: UsersCollectionRepository
) {

    /**
     * Returns TO THE FIRST household ID found in expensesRepository.getCurrentHouseholdIds.
     * For now only 1 household will be allowed by user, but in the future we might need to add logic
     * if we want more than 1 household per user. The data will already support it, but not our logic
     */
    suspend fun getCurrentHousehold(userId: String): UserHousehold? {
        return usersCollectionRepository.getUserHouseholds(userId).ifEmpty { null }?.first()
    }

    suspend fun createHousehold(
        userId: String,
        houseHoldName: String
    ): Result<UserHousehold> {
        val householdCreationResult = householdRepository.createHousehold(userId, houseHoldName)
        return householdCreationResult.fold(
            onSuccess = { household ->
                val addHouseholdToUserResult = usersCollectionRepository.addHouseholdToUser(userId, household)

                addHouseholdToUserResult.fold(
                    onSuccess = { Result.success(household) },
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
        val currentHousehold = getCurrentHousehold(userData.id)
        return if (currentHousehold == null) {
            return Result.failure(HouseholdNotFoundException())
        } else {
            householdRepository.addExpenseToHousehold(userData, currentHousehold.id, expense)
        }
    }

    /**
     * TODO: FirestoreExpense shouldnt be used in domain layer being a data object
     */
    suspend fun getAllExpensesFromHousehold(householdId: String): Result<List<FirestoreExpense>> {
        return householdRepository.getAllExpensesFromHousehold(householdId)
    }
}
