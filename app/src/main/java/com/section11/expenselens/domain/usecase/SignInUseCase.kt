package com.section11.expenselens.domain.usecase

import androidx.credentials.GetCredentialResponse
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.AuthenticationRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val userSessionRepository: UserSessionRepository,
    private val usersCollectionRepository: UsersCollectionRepository
) {

    suspend fun signInCredentialsFetched(credentialResponse: GetCredentialResponse): Result<UserData> {
        val signInResult = authenticationRepository.signInCredentialsFetched(credentialResponse)

        return signInResult.fold(
            onSuccess = { handleSignInSuccess(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun handleSignInSuccess(userData: UserData): Result<UserData> {
        return with(userData) {
            val firestoreUserResult = usersCollectionRepository.createOrUpdateUser(userData)

            firestoreUserResult.fold(
                onSuccess = {
                    userSessionRepository.saveUser(this)
                    Result.success(userData)
                },
                onFailure = { Result.failure(it) }
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
        userSessionRepository.clearUser()
        authenticationRepository.signOut()
    }

    suspend fun updateCurrentHouseholdId(householdId: String) {
        userSessionRepository.updateCurrentHouseholdId(householdId)
    }
}
