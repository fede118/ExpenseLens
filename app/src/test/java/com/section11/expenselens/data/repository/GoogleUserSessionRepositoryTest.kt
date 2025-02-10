package com.section11.expenselens.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleUserSessionRepositoryTest {

    private val prefs: Preferences = mock()
    private val dataStorePreferences: DataStore<Preferences> = mock()
    private val tokenPrefKey = stringPreferencesKey("token")
    private val userIdPrefKey = stringPreferencesKey("userId")
    private val displayNamePrefKey = stringPreferencesKey("displayName")
    private val profilePicPrefKey = stringPreferencesKey("profilePic")

    private lateinit var repository: GoogleUserSessionRepository

    @Before
    fun setup() {
        repository = GoogleUserSessionRepository(
            dataStorePreferences,
            tokenPrefKey,
            userIdPrefKey,
            displayNamePrefKey,
            profilePicPrefKey
        )
    }

    @Test
    fun `saveUser should store user data in preferences`() = runTest {
        // Given
        val idToken = "testToken"
        val id = "user123"
        val name = "John Doe"
        val profilePic: Uri = mock()

        // When
        repository.saveUser(idToken, id, name, profilePic)

        verify(dataStorePreferences).edit(anyOrNull())
    }

    @Test
    fun `saveUser should save user data to preferences`() = runTest {
        // Arrange
        val idToken = "testToken"
        val id = "testId"
        val name = "testName"
        val profilePic = mock<Uri>()
        val profilePicString = "testProfilePic"
        whenever(profilePic.toString()).thenReturn(profilePicString)
        // Act
        repository.saveUser(idToken, id, name, profilePic)
        advanceUntilIdle()

        val captor = argumentCaptor<suspend(MutablePreferences) -> Unit>()
        // Assert
        verify(dataStorePreferences).edit(captor.capture())
    }

    @Test
    fun `getUser should return user data when stored`() = runTest {
        // Given
        whenever(dataStorePreferences.data).thenReturn(flowOf(prefs))
        whenever(prefs[tokenPrefKey]).thenReturn("testToken")
        whenever(prefs[userIdPrefKey]).thenReturn("user123")
        whenever(prefs[displayNamePrefKey]).thenReturn("John Doe")
        whenever(prefs[profilePicPrefKey]).thenReturn("https://example.com/profile.jpg")

        // When
        val user = repository.getUser()

        // Assert
        assertNotNull(user)
        assertEquals("testToken", user?.idToken)
        assertEquals("user123", user?.id)
        assertEquals("John Doe", user?.displayName)
        assertEquals("https://example.com/profile.jpg", user?.profilePic)
    }

    @Test
    fun `getUser should return null if token or id is missing`() = runTest {
        // Given
        whenever(dataStorePreferences.data).thenReturn(flowOf(prefs))
        whenever(prefs[tokenPrefKey]).thenReturn(null)
        whenever(prefs[userIdPrefKey]).thenReturn(null)

        // When
        val user = repository.getUser()

        // Then
        assertNull(user)
    }

    @Test
    fun `clearUser should clear preferences`() = runTest {
        // When
        repository.clearUser()

        // Then
        verify(dataStorePreferences).edit(anyOrNull())
    }
}
