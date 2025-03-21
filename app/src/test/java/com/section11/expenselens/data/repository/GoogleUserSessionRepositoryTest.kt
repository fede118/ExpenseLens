package com.section11.expenselens.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.section11.expenselens.domain.models.UserData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleUserSessionRepositoryTest {

    private val tokenPrefKey = stringPreferencesKey("token")
    private val userIdPrefKey = stringPreferencesKey("userId")
    private val displayNamePrefKey = stringPreferencesKey("displayName")
    private val profilePicPrefKey = stringPreferencesKey("profilePic")
    private val emailPrefKey = stringPreferencesKey("email")
    private val notificationTokenPrefKey = stringPreferencesKey("notificationToken")
    private val currentHouseholdIdKey = stringPreferencesKey("currentHouseholdId")
    private val testScope = TestScope()
    
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var repository: GoogleUserSessionRepository

    @Before
    fun setup() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope.backgroundScope,
            produceFile = { Files.createTempFile("test_prefs", ".preferences_pb").toFile() }
        )
        
        repository = GoogleUserSessionRepository(
            testDataStore,
            tokenPrefKey,
            userIdPrefKey,
            displayNamePrefKey,
            profilePicPrefKey,
            emailPrefKey,
            notificationTokenPrefKey,
            currentHouseholdIdKey
        )
    }

    @Test
    fun `saveUser should save user data to preferences`() = testScope.runTest {
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

        // Then
        val prefs = testDataStore.data.first()
        assertEquals("testToken", prefs[tokenPrefKey])
        assertEquals("user123", prefs[userIdPrefKey])
        assertEquals("John Doe", prefs[displayNamePrefKey])
        assertEquals("ProfilePic", prefs[profilePicPrefKey])
        assertEquals("email", prefs[emailPrefKey])
        assertEquals("notificationToken", prefs[notificationTokenPrefKey])
    }

    @Test
    fun `getUser should return user data when stored`() = testScope.runTest {
        // Given
        testDataStore.edit { prefs ->
            prefs[tokenPrefKey] = "testToken"
            prefs[userIdPrefKey] = "user123"
            prefs[displayNamePrefKey] = "John Doe"
            prefs[profilePicPrefKey] = "https://example.com/profile.jpg"
            prefs[emailPrefKey] = "email"
            prefs[notificationTokenPrefKey] = "notificationToken"
            prefs[currentHouseholdIdKey] = "householdId"
        }

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
        assertEquals("householdId", user?.currentHouseholdId)
    }

    @Test
    fun `getUser should return null if token or id is missing`() = testScope.runTest {
        // Given
        testDataStore.edit {
            it.remove(tokenPrefKey)
            it.remove(userIdPrefKey)
            it[displayNamePrefKey] = "John Doe"
            it[profilePicPrefKey] = "https://example.com/profile.jpg"
        }

        // When
        val user = repository.getUser()

        // Then
        assertNull(user)
    }

    @Test
    fun `clearUser should clear preferences`() = testScope.runTest {
        // When
        repository.clearUser()

        // Then
        val prefs = testDataStore.data.first()
        assertFalse(prefs.contains(userIdPrefKey))
        assertFalse(prefs.contains(tokenPrefKey))
        assertFalse(prefs.contains(displayNamePrefKey))
        assertFalse(prefs.contains(profilePicPrefKey))
        assertFalse(prefs.contains(emailPrefKey))
        assertFalse(prefs.contains(notificationTokenPrefKey))
    }

    @Test
    fun `updateNotificationToken should update notification token in preferences`() = testScope.runTest {
        // Given
        val newToken = "newToken"

        // When
        repository.updateNotificationToken(newToken)
        advanceUntilIdle()

        // Then
        val prefs = testDataStore.data.first()
        assert(prefs[notificationTokenPrefKey] == newToken)
    }
    
    @Test
    fun `updateCurrentHouseholdId should remove current household id from preferences`() = testScope.runTest {
        // Given
        testDataStore.edit { it[currentHouseholdIdKey] = "testHouseholdId" }

        // When
        repository.updateCurrentHouseholdId(null)

        // Then
        val prefs = testDataStore.data.first()
        assertFalse(prefs.contains(currentHouseholdIdKey))
    }

    @Test
    fun `updateCurrentHouseholdId should save current household id to preferences`() = testScope.runTest {
        // When
        repository.updateCurrentHouseholdId("newHouseholdId")

        // Then
        val prefs = testDataStore.data.first()
        assertEquals("newHouseholdId", prefs[currentHouseholdIdKey])
    }
}
