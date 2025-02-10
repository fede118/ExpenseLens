package com.section11.expenselens.domain.repository

import android.net.Uri
import com.section11.expenselens.domain.models.UserData

interface UserSessionRepository {

    suspend fun saveUser(idToken: String, id: String, name: String?, profilePic: Uri?)

    suspend fun getUser(): UserData?

    suspend fun clearUser()
}
