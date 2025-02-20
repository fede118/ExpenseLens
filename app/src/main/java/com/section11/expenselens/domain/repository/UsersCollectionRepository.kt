package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.UserHousehold

interface UsersCollectionRepository {

    suspend fun createUserIfNotExists(userId: String, email: String): Result<Unit>

    suspend fun getUserHouseholds(userId: String): List<UserHousehold>

    suspend fun addHouseholdToUser(userId: String, household: UserHousehold): Result<Unit>
}
