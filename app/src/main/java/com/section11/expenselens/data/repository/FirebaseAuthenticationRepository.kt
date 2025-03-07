package com.section11.expenselens.data.repository

import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.section11.expenselens.domain.exceptions.IllegalUserInfoException
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.exceptions.InvalidCredentialTypeException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.AuthenticationRepository
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val FIREBASE_AUTH_EXCEPTION_MESSAGE = "Firebase authentication failed"
private const val FIREBASE_EXCEPTION_ID_NULL = "User ID or Email is null after successful Firebase " +
        "sign-in. This is unexpected."
private const val FIREBASE_EXCEPTION_EMAIL_NULL = "User email is null after successful Firebase " +
        "sign-in. This is unexpected."
/**
 *  I don't like that the credential manager needs context to create the request. It should be
 *  injected when getting the [CredentialManager] instance so that I can just inject my credential
 *  manager and not have Context in this class. I could have a "Context Provider" or a service for
 *  this but it wouldn't actually make the data layer agnostic of context so this is just simpler if
 *  its still going to be wrong
 */
class FirebaseAuthenticationRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val googleTokenMapper: GoogleTokenMapper
) : AuthenticationRepository {

    /**
     * Method to show the google sign in and retrieve and store a token
     *
     * This method swallows the GetCredentialCancellationException.
     * GetCredentialCancellationException means the user cancelled the sign in, swallowing the
     * exception makes sense
     */
    override suspend fun signInCredentialsFetched(credentialResponse: GetCredentialResponse): Result<UserData> {
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

    private suspend fun firebaseAuthWithGoogle(data: Bundle): Result<UserData> {
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
                with(googleUser) {
                    Result.success(
                        UserData(
                            idToken,
                            uid,
                            displayName,
                            profilePictureUri.toString(),
                            email,
                            firebaseMessaging.token.await()
                        )
                    )
                }
            }
        } else {
            Result.failure(
                IllegalUserInfoException(FIREBASE_AUTH_EXCEPTION_MESSAGE)
            )
        }
    }


    override fun signOut() {
        firebaseAuth.signOut()
    }
}
