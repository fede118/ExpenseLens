package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.UserData

interface UserSessionRepository {

    suspend fun saveUser(userData: UserData)

    suspend fun getUser(): UserData?

    suspend fun clearUser()
}
