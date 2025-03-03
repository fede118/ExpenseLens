package com.section11.expenselens.data.repository

import android.net.Uri
import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.section11.expenselens.domain.exceptions.IllegalUserInfoException
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthenticationRepositoryTest {

    private lateinit var repository: FirebaseAuthenticationRepository
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseMessaging: FirebaseMessaging = mock()
    private val mockTokenMapper: GoogleTokenMapper = mock()

    @Before
    fun setUp() {
        repository = FirebaseAuthenticationRepository(
            firebaseAuth,
            firebaseMessaging,
            mockTokenMapper
        )
    }

    @Test
    fun `signInToGoogle with valid Google ID token should return user data`() = runTest {
        val token = "valid_token"
        val id = "valid_id"
        val displayName = "valid_display_name"
        val profilePictureUri: Uri = mock()
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        val mockGoogleToken = mockGoogleToken(id, displayName, profilePictureUri, token)
        whenever(mockTokenMapper.toGoogleToken(mockBundle)).thenReturn(mockGoogleToken)
        val mockTask: Task<AuthResult> = Tasks.forResult(mock())
        whenever(firebaseAuth.signInWithCredential(any())).thenReturn(mockTask)
        val userMock = getFireBaseUser(id)
        whenever(userMock.email).thenReturn("email")
        whenever(firebaseAuth.currentUser).thenReturn(userMock)
        whenever(firebaseMessaging.token).thenReturn(Tasks.forResult("mock_token"))

        val result = repository.signInCredentialsFetched(credentialResponse)
        advanceUntilIdle()

        assert(result.isSuccess)
        val userData = result.getOrNull()
        userData?.run {
            assert(token == token)
            assert(id == id)
            assert(displayName == displayName)
        } ?: throw NullPointerException("test failed, user data is null")
    }

    @Test
    fun `signInToGoogle with invalid Credential should return failure`() = runTest {
        val mockCredentialResponse: GetCredentialResponse = mock()
        val mockCredential: Credential = mock() // credential is not Custom credential
        whenever(mockCredentialResponse.credential).thenReturn(mockCredential)

        val result = repository.signInCredentialsFetched(mockCredentialResponse)
        advanceUntilIdle()

        assert(result.isFailure)
    }

    @Test
    fun `signInToGoogle with custom credential but wrong type should return failure`() = runTest {
        val mockCredentialResponse: GetCredentialResponse = mock()
        val mockCredential: CustomCredential = mock()
        whenever(mockCredentialResponse.credential).thenReturn(mockCredential)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL)

        val result = repository.signInCredentialsFetched(mockCredentialResponse)
        advanceUntilIdle()

        assert(result.isFailure)
    }

    @Test
    fun `when firebase user id is null should return failure`() = runTest {
        val token = "valid_token"
        val id = "valid_id"
        val displayName = "valid_display_name"
        val profilePictureUri: Uri = mock()
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        val mockGoogleToken = mockGoogleToken(id, displayName, profilePictureUri, token)
        whenever(mockTokenMapper.toGoogleToken(mockBundle)).thenReturn(mockGoogleToken)
        val mockTask: Task<AuthResult> = Tasks.forResult(mock())
        whenever(firebaseAuth.signInWithCredential(any())).thenReturn(mockTask)
        val userMock = getFireBaseUser()
        whenever(firebaseAuth.currentUser).thenReturn(userMock)

        val result = repository.signInCredentialsFetched(credentialResponse)
        advanceUntilIdle()

        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalUserInfoException)
    }

    @Test
    fun `when firebase user email is null should return failure`() = runTest {
        val token = "valid_token"
        val id = "valid_id"
        val displayName = "valid_display_name"
        val profilePictureUri: Uri = mock()
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        val mockGoogleToken = mockGoogleToken(id, displayName, profilePictureUri, token)
        whenever(mockTokenMapper.toGoogleToken(mockBundle)).thenReturn(mockGoogleToken)
        val mockTask: Task<AuthResult> = Tasks.forResult(mock())
        whenever(firebaseAuth.signInWithCredential(any())).thenReturn(mockTask)
        val userMock = getFireBaseUser("someUid")
        whenever(firebaseAuth.currentUser).thenReturn(userMock)
        whenever(userMock.email).thenReturn(null)

        val result = repository.signInCredentialsFetched(credentialResponse)
        advanceUntilIdle()

        assert(result.isFailure)
        assert(result.exceptionOrNull() is IllegalUserInfoException)
    }

    private fun mockGetCredentialResponse(bundle: Bundle): GetCredentialResponse {
        val mockCredential: CustomCredential = mock()
        whenever(mockCredential.data).thenReturn(bundle)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val mockGetCredentialResponse: GetCredentialResponse = mock()
        whenever(mockGetCredentialResponse.credential).thenReturn(mockCredential)
        return mockGetCredentialResponse
    }

    private fun getFireBaseUser(uid: String? = null): FirebaseUser {
        val mockUser: FirebaseUser = mock()
        uid?.let { whenever(mockUser.uid).thenReturn(it) }
        return mockUser
    }

    private fun mockGoogleToken(
        id: String,
        displayName: String,
        profilePictureUri: Uri,
        idToken: String
    ): GoogleIdTokenCredential {
        val mock: GoogleIdTokenCredential = mock()
        mock.let {
            whenever(it.idToken).thenReturn(idToken)
            whenever(it.displayName).thenReturn(displayName)
            whenever(it.profilePictureUri).thenReturn(profilePictureUri)
            whenever(it.id).thenReturn(id)
        }
        return mock
    }
}
