package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.exceptions.HouseholdNotFoundException
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import javax.inject.Inject

class HouseholdDetailsUseCase @Inject constructor(
    private val householdRepository: HouseholdRepository,
    private val usersCollectionRepository: UsersCollectionRepository,
    private val userSessionRepository: UserSessionRepository
) {

    suspend fun getCurrentHouseholdDetails(): Result<HouseholdDetailsWithUserEmails> {
        val userData = userSessionRepository.getUser()
        return if (userData?.currentHouseholdId == null) {
            Result.failure(HouseholdNotFoundException())
        } else {
            householdRepository.getHouseholdDetails(userData.currentHouseholdId).fold(
                onFailure = { Result.failure(it) },
                onSuccess = {
                    val userEmails = usersCollectionRepository.getListOfUserEmails(it.usersIds)
                    Result.success(
                        HouseholdDetailsWithUserEmails(userData.id, it.id, it.name, userEmails)
                    )
                }
            )
        }
    }

    suspend fun leaveHousehold(userId: String, householdId: String): Result<Unit> {
        usersCollectionRepository.removeHouseholdFromUser(userId, householdId)
        householdRepository.removeUserFromHousehold(userId, householdId)
        val userHouseholds = usersCollectionRepository.getUserHouseholds(userId)
        if (userHouseholds.isEmpty()) {
            userSessionRepository.updateCurrentHouseholdId(null)
        } else {
            userSessionRepository.updateCurrentHouseholdId(userHouseholds.first().id)
        }
        return Result.success(Unit)
    }

    suspend fun deleteHousehold(userId: String, householdId: String): Result<Unit> {
        return usersCollectionRepository.removeHouseholdFromUser(userId, householdId)
            .fold(
                onSuccess = { householdRepository.deleteHousehold(userId, householdId) },
                onFailure = { Result.failure(it) }
            )
    }
}
