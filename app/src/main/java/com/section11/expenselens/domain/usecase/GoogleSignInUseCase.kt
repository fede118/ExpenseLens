package com.section11.expenselens.domain.usecase

import android.content.Context
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.section11.expenselens.domain.exceptions.IllegalUserInfoException
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.exceptions.InvalidCredentialTypeException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInSuccess
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val FIREBASE_AUTH_EXCEPTION_MESSAGE = "Firebase authentication failed"
private const val FIREBASE_EXCEPTION_ID_NULL = "User ID or Email is null after successful Firebase " +
        "sign-in. This is unexpected."
private const val FIREBASE_EXCEPTION_EMAIL_NULL = "User email is null after successful Firebase " +
        "sign-in. This is unexpected."
private const val FIREBASE_EXCEPTION_USER_CREATION_FAILED = "Failed to add user to users collection"

class GoogleSignInUseCase @Inject constructor(
    private val credentialRequest: GetCredentialRequest,
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth,
    private val userSessionRepository: UserSessionRepository,
    private val googleTokenMapper: GoogleTokenMapper,
    private val usersCollectionRepository: UsersCollectionRepository
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
            return Result.success(SignInResult.SignInCancelled)
        }

        return when (credentialResponse.credential) {
            is CustomCredential -> {
                if (credentialResponse.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    firebaseAuthWithGoogle(credentialResponse.credential.data)
                } else {
                    Result.failure(InvalidCredentialTypeException())
                }
            }
            else -> Result.failure(InvalidCredentialException())
        }
    }

    private suspend fun firebaseAuthWithGoogle(data: Bundle): Result<SignInResult> {
        val googleUser = googleTokenMapper.toGoogleToken(data)
        val credential = GoogleAuthProvider.getCredential(googleUser.idToken, null)

        val task = firebaseAuth.signInWithCredential(credential)
        task.await()

        return if (task.isSuccessful) {
            val user = firebaseAuth.currentUser
            val uid = user?.uid
            val email = user?.email
            return if (uid == null  ) {
                Result.failure(
                    IllegalUserInfoException(FIREBASE_EXCEPTION_ID_NULL)
                )
            } else if (email == null) {
                Result.failure(
                    IllegalUserInfoException(FIREBASE_EXCEPTION_EMAIL_NULL)
                )
            } else {
                val firestoreUserResult = usersCollectionRepository.createUserIfNotExists(uid, email)
                if (firestoreUserResult.isFailure) {
                    throw IllegalUserInfoException(FIREBASE_EXCEPTION_USER_CREATION_FAILED)
                }

                with(googleUser) {
                    userSessionRepository.saveUser(
                        idToken,
                        uid,
                        displayName,
                        profilePictureUri
                    )
                    Result.success(SignInSuccess(
                        UserData(
                            idToken,
                            uid,
                            displayName,
                            profilePictureUri.toString()
                        )
                    ))
                }
            }
        } else {
            Result.failure(
                IllegalUserInfoException(FIREBASE_AUTH_EXCEPTION_MESSAGE)
            )
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
        firebaseAuth.signOut()
        userSessionRepository.clearUser()
    }

    sealed class SignInResult {
        data class SignInSuccess(val userData: UserData) : SignInResult()
        data object SignInCancelled: SignInResult()
    }
}
