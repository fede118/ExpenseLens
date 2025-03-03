package com.section11.expenselens.domain.repository

import androidx.credentials.GetCredentialResponse
import com.section11.expenselens.domain.models.UserData

interface AuthenticationRepository {

    suspend fun signInCredentialsFetched(credentialResponse: GetCredentialResponse): Result<UserData>

    fun signOut()

}
