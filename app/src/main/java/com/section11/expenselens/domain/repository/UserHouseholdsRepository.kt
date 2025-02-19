package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.UserHousehold

interface UserHouseholdsRepository {

    suspend fun getUserHouseholds(userId: String): List<UserHousehold>

    suspend fun addHouseholdToUser(userId: String, household: UserHousehold): Result<Unit>
}
