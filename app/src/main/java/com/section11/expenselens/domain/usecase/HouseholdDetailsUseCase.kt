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
                    Result.success(HouseholdDetailsWithUserEmails(it.id, it.name, userEmails))
                }
            )
        }
    }
}
