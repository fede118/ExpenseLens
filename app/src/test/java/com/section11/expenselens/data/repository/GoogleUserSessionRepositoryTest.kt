package com.section11.expenselens.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.section11.expenselens.domain.models.UserData
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
    private val emailPrefKey = stringPreferencesKey("email")
    private val notificationTokenPrefKey = stringPreferencesKey("notificationToken")

    private lateinit var repository: GoogleUserSessionRepository

    @Before
    fun setup() {
        repository = GoogleUserSessionRepository(
            dataStorePreferences,
            tokenPrefKey,
            userIdPrefKey,
            displayNamePrefKey,
            profilePicPrefKey,
            emailPrefKey,
            notificationTokenPrefKey
        )
    }

    @Test
    fun `saveUser should store user data in preferences`() = runTest {
        // Given
        val user = UserData(
            "testToken",
            "user123",
            "John Doe",
            "ProfilePic",
            "email",
            "notificationToken"
        )

        // When
        repository.saveUser(user)

        verify(dataStorePreferences).edit(anyOrNull())
    }

    @Test
    fun `saveUser should save user data to preferences`() = runTest {
        // Given
        val user = UserData(
            "testToken",
            "user123",
            "John Doe",
            "ProfilePic",
            "email",
            "notificationToken"
        )
        // When
        repository.saveUser(user)
        advanceUntilIdle()

        val captor = argumentCaptor<suspend(MutablePreferences) -> Unit>()
        // Then
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
        whenever(prefs[emailPrefKey]).thenReturn("email")
        whenever(prefs[notificationTokenPrefKey]).thenReturn("notificationToken")

        // When
        val user = repository.getUser()

        // Assert
        assertNotNull(user)
        assertEquals("testToken", user?.idToken)
        assertEquals("user123", user?.id)
        assertEquals("John Doe", user?.displayName)
        assertEquals("https://example.com/profile.jpg", user?.profilePic)
        assertEquals("email", user?.email)
        assertEquals("notificationToken", user?.notificationToken)
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

    @Test
    fun `updateNotificationToken should update notification token in preferences`() = runTest {
        // Given
        val newToken = "newToken"

        // When
        repository.updateNotificationToken(newToken)
        advanceUntilIdle()

        // Then
        verify(dataStorePreferences).edit(anyOrNull())
    }
}
