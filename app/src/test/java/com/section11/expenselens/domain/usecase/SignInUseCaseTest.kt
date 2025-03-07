package com.section11.expenselens.domain.usecase

import android.os.Bundle
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuthException
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.repository.AuthenticationRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import com.section11.expenselens.ui.utils.getUserData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SignInUseCaseTest {

    private val userSessionRepository: UserSessionRepository = mock()
    private val usersCollectionRepository: UsersCollectionRepository = mock()
    private val authenticationRepository: AuthenticationRepository = mock()
    private lateinit var signInUseCase: SignInUseCase

    @Before
    fun setup() {
        signInUseCase = SignInUseCase(
            authenticationRepository,
            userSessionRepository,
            usersCollectionRepository
        )
    }

    @Test
    fun `signInToGoogle when Firebase sign-in fails should return failure`() = runTest {
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        val firebaseAuthException: FirebaseAuthException = mock()
        whenever(firebaseAuthException.errorCode).thenReturn("ERROR_CODE")
        whenever(firebaseAuthException.message).thenReturn("Mock authentication error")
        whenever(authenticationRepository.signInCredentialsFetched(any())).thenReturn(
            Result.failure(firebaseAuthException)
        )

        val result = signInUseCase.signInCredentialsFetched(credentialResponse)
        advanceUntilIdle()

        assert(result.isFailure)
        val exception = result.exceptionOrNull()
        assert(exception is FirebaseAuthException)
        assert(exception?.message == "Mock authentication error")
    }

    @Test
    fun `signInToGoogle when Firebase sign-in succeeds should return user data`() = runTest {
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        val userData = getUserData()
        whenever(authenticationRepository.signInCredentialsFetched(any())).thenReturn(
            Result.success(userData)
        )

        val result = signInUseCase.signInCredentialsFetched(credentialResponse)
        advanceUntilIdle()

        assert(result.isSuccess)
        assert(result.getOrNull() == userData)
    }

    @Test
    fun `on sign out the firebase should sign out and db should be cleared`() = runTest {
        signInUseCase.signOut()

        verify(userSessionRepository).clearUser()
        verify(authenticationRepository).signOut()
    }

    @Test
    fun `on get currentUser the user should be returned`() = runTest {
        val userData = getUserData()
        whenever(userSessionRepository.getUser()).thenReturn(userData)

        val result = signInUseCase.getCurrentUser()

        assert(result.isSuccess)
        assert(result.getOrNull() == userData)
    }

    @Test
    fun `on get currentUser the user should not be returned`() = runTest {
        whenever(userSessionRepository.getUser()).thenReturn(null)

        val result = signInUseCase.getCurrentUser()

        assert(result.isFailure)
        assert(result.exceptionOrNull() is InvalidCredentialException)
    }

    @Test
    fun `getCurrentUser with existing user should return user data`() = runTest {
        val userData = getUserData()
        whenever(userSessionRepository.getUser()).thenReturn(userData)

        val result = signInUseCase.getCurrentUser()
        assert(result.isSuccess)
        assert(result.getOrNull() == userData)
    }

    @Test
    fun `getCurrentUser with no user should return failure`() = runTest {
        whenever(userSessionRepository.getUser()).thenReturn(null)

        val result = signInUseCase.getCurrentUser()
        assert(result.isFailure)
        assert(result.exceptionOrNull() is InvalidCredentialException)
    }

    @Test
    fun `signOut should clear user data`() = runTest {
        signInUseCase.signOut()
        verify(userSessionRepository).clearUser()
    }

    @Test
    fun `updateCurrentHouseholdId should update the current household id`() = runTest {
        val householdId = "householdId"

        signInUseCase.updateCurrentHouseholdId(householdId)

        verify(userSessionRepository).updateCurrentHouseholdId(householdId)
    }

    private fun mockGetCredentialResponse(bundle: Bundle): GetCredentialResponse {
        val mockCredential: CustomCredential = mock()
        whenever(mockCredential.data).thenReturn(bundle)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val mockGetCredentialResponse: GetCredentialResponse = mock()
        whenever(mockGetCredentialResponse.credential).thenReturn(mockCredential)
        return mockGetCredentialResponse
    }
}
