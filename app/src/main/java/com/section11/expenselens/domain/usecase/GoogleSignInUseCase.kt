package com.section11.expenselens.domain.usecase

import android.content.Context
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.exceptions.InvalidCredentialTypeException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
    private val userSessionRepository: UserSessionRepository,
    private val googleTokenMapper: GoogleTokenMapper
) {

    suspend fun signInToGoogle(context: Context): Result<UserData> {
        val credentialResponse = credentialManager.getCredential(context, credentialRequest)

        return when (credentialResponse.credential) {
            is CustomCredential -> {
                if (credentialResponse.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    tryGettingUserDataAndStore(credentialResponse.credential.data)
                } else {
                    Result.failure(InvalidCredentialTypeException())
                }
            }
            else -> Result.failure(InvalidCredentialException())
        }
    }

    private suspend fun tryGettingUserDataAndStore(data: Bundle): Result<UserData> {
        return try {
            val googleUser = googleTokenMapper.toGoogleToken(data)
            with(googleUser) {
                userSessionRepository.saveUser(idToken, id, displayName, profilePictureUri)
                Result.success(UserData(idToken, id, displayName, profilePictureUri.toString()))
            }
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserData> {
        val user = userSessionRepository.getUser()
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(InvalidCredentialException())
        }
    }

    suspend fun signOut() {
        userSessionRepository.clearUser()
    }
}
