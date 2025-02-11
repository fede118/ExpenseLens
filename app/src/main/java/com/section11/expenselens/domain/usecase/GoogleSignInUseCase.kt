package com.section11.expenselens.domain.usecase

import android.content.Context
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.exceptions.InvalidCredentialTypeException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInSuccess
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
    private val userSessionRepository: UserSessionRepository,
    private val googleTokenMapper: GoogleTokenMapper
) {

    /**
     * Method to show the google sign in and retrieve and store a token
     *
     * This method swallows the GetCredentialCancellationException.
     * GetCredentialCancellationException means the user cancelled the sign in, swallowing the
     * exception makes sense
     */
    @Suppress("SwallowedException")
    suspend fun signInToGoogle(context: Context): Result<SignInResult> {
        val credentialResponse = try {
             credentialManager.getCredential(context, credentialRequest)
        } catch (cancellationException: GetCredentialCancellationException) {
            // The process was successful in the sense that nothing went wrong, just that the user cancelled the sign in
            return Result.success(SignInResult.SignInCancelled)
        }

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

    private suspend fun tryGettingUserDataAndStore(data: Bundle): Result<SignInResult> {
        return try {
            val googleUser = googleTokenMapper.toGoogleToken(data)
            with(googleUser) {
                userSessionRepository.saveUser(idToken, id, displayName, profilePictureUri)
                Result.success(SignInSuccess(
                    UserData(
                        idToken,
                        id,
                        displayName,
                        profilePictureUri.toString()
                    )
            ))
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

    sealed class SignInResult {
        data class SignInSuccess(val userData: UserData) : SignInResult()
        data object SignInCancelled: SignInResult()
    }
}
