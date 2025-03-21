package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold

interface UsersCollectionRepository {

    suspend fun createOrUpdateUser(userData: UserData): Result<Unit>

    suspend fun getUserHouseholds(userId: String): List<UserHousehold>

    suspend fun addHouseholdToUser(userId: String, household: UserHousehold): Result<Unit>

    suspend fun removeHouseholdFromUser(userId: String, householdId: String): Result<Unit>

    suspend fun updateNotificationToken(userId: String, newToken: String): Result<Unit>

    suspend fun getListOfUserEmails(userIds: List<String>): List<String>
}
