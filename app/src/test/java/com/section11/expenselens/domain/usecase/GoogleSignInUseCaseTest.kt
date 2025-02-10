package com.section11.expenselens.domain.usecase

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.section11.expenselens.domain.exceptions.InvalidCredentialException
import com.section11.expenselens.domain.exceptions.InvalidCredentialTypeException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class GoogleSignInUseCaseTest {

    private val credentialRequest: GetCredentialRequest = mock()
    private val credentialManager: CredentialManager = mock()
    private val userSessionRepository: UserSessionRepository = mock()
    private val mockTokenMapper: GoogleTokenMapper = mock()
    private lateinit var googleSignInUseCase: GoogleSignInUseCase

    @Before
    fun setup() {
        googleSignInUseCase = GoogleSignInUseCase(
            credentialRequest,
            credentialManager,
            userSessionRepository,
            mockTokenMapper
        )
    }

    @Test
    fun `signInToGoogle with valid Google ID token should return user data`() = runTest {
        val token = "valid_token"
        val id = "valid_id"
        val displayName = "valid_display_name"
        val profilePictureUri: Uri = mock()
        val context: Context = mock()
        val mockBundle: Bundle = mock()
        whenever(mockBundle.getString(anyString())).thenReturn("mock string")
        val credentialResponse = mockGetCredentialResponse(mockBundle)
        whenever(credentialManager.getCredential(context, credentialRequest)).thenReturn(credentialResponse)
        whenever(userSessionRepository.saveUser(anyString(), anyString(), anyString(), any())).thenReturn(Unit)
        val mockGoogleToken = mockGoogleToken(id, displayName, profilePictureUri, token)
        whenever(mockTokenMapper.toGoogleToken(mockBundle)).thenReturn(mockGoogleToken)

        val result = googleSignInUseCase.signInToGoogle(context)

        assert(result.isSuccess)
        val userData = result.getOrNull()
        userData?.run {
            assert(token == token)
            assert(id == id)
            assert(displayName == displayName)
        } ?: throw NullPointerException("test failed, user data is null")
    }

    @Test
    fun `signInToGoogle with invalid credential type should return failure`() = runTest {
        val context = mock(Context::class.java)
        val customCredential = CustomCredential("invalid_type", mock(Bundle::class.java))
        val credentialResponse = GetCredentialResponse(customCredential)

        whenever(credentialManager.getCredential(context, credentialRequest)).thenReturn(credentialResponse)

        val result = googleSignInUseCase.signInToGoogle(context)
        assert(result.isFailure)
        assert(result.exceptionOrNull() is InvalidCredentialTypeException)
    }

    @Test
    fun `signInToGoogle with invalid credential should return failure`() = runTest {
        val context = mock(Context::class.java)
        val credentialResponse = GetCredentialResponse(mock())

        whenever(credentialManager.getCredential(context, credentialRequest)).thenReturn(credentialResponse)

        val result = googleSignInUseCase.signInToGoogle(context)
        assert(result.isFailure)
        assert(result.exceptionOrNull() is InvalidCredentialException)
    }

    @Test
    fun `getCurrentUser with existing user should return user data`() = runTest {
        val userData = UserData("token", "id", "displayName", "profilePictureUri")
        whenever(userSessionRepository.getUser()).thenReturn(userData)

        val result = googleSignInUseCase.getCurrentUser()
        assert(result.isSuccess)
        assert(result.getOrNull() == userData)
    }

    @Test
    fun `getCurrentUser with no user should return failure`() = runTest {
        whenever(userSessionRepository.getUser()).thenReturn(null)

        val result = googleSignInUseCase.getCurrentUser()
        assert(result.isFailure)
        assert(result.exceptionOrNull() is InvalidCredentialException)
    }

    @Test
    fun `signOut should clear user data`() = runTest {
        googleSignInUseCase.signOut()
        verify(userSessionRepository).clearUser()
    }

    private fun mockGoogleToken(
        id: String,
        displayName: String,
        profilePictureUri: Uri,
        idToken: String
    ): GoogleIdTokenCredential {
        val mock: GoogleIdTokenCredential = mock()
        mock.let {
            whenever(it.idToken).thenReturn(id)
            whenever(it.displayName).thenReturn(displayName)
            whenever(it.profilePictureUri).thenReturn(profilePictureUri)
            whenever(it.id).thenReturn(id)
        }
        return mock
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
