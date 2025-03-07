package com.section11.expenselens.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Suppress("LongParameterList") // This is a DataStore, it needs all the keys
class GoogleUserSessionRepository @Inject constructor(
    private val dataStorePreferences: DataStore<Preferences>,
    private val tokenPrefKey: Preferences.Key<String>,
    private val userIdPrefKey: Preferences.Key<String>,
    private val displayNamePrefKey: Preferences.Key<String>,
    private val profilePicPrefKey: Preferences.Key<String>,
    private val emailPrefKey: Preferences.Key<String>,
    private val notificationTokenPrefKey: Preferences.Key<String>,
    private val currentHouseholdIdPrefKey: Preferences.Key<String>
) : UserSessionRepository {

    override suspend fun saveUser(userData: UserData) {
        with(userData) {
            dataStorePreferences.edit { prefs ->
                prefs[tokenPrefKey] = idToken
                prefs[userIdPrefKey] = id
                prefs[emailPrefKey] = email
                prefs[notificationTokenPrefKey] = notificationToken
                currentHouseholdId?.let { prefs[currentHouseholdIdPrefKey] = it }
                displayName?.let { prefs[displayNamePrefKey] = it }
                profilePic?.let { prefs[profilePicPrefKey] = it }
            }
        }
    }

    override suspend fun getUser(): UserData? {
        val prefs = dataStorePreferences.data.first()
        val idToken = prefs[tokenPrefKey].orEmpty()
        val id = prefs[userIdPrefKey].orEmpty()
        val email = prefs[emailPrefKey].orEmpty()
        val notificationToken = prefs[notificationTokenPrefKey].orEmpty()
        val displayName = prefs[displayNamePrefKey]
        val profilePic = prefs[profilePicPrefKey]
        val currentHouseholdId = prefs[currentHouseholdIdPrefKey]
        return  if (listOf(idToken, id, email, notificationToken).any { it.isEmpty() }) {
            null
        } else {
            UserData(
                idToken = idToken,
                id = id,
                displayName = displayName,
                profilePic = profilePic,
                email = email,
                notificationToken = notificationToken,
                currentHouseholdId = currentHouseholdId
            )
        }
    }

    override suspend fun clearUser() {
        dataStorePreferences.edit { it.clear() }
    }

    override suspend fun updateNotificationToken(newToken: String) {
        dataStorePreferences.edit { prefs ->
            prefs[notificationTokenPrefKey] = newToken
        }
    }

    override suspend fun updateCurrentHouseholdId(householdId: String) {
        dataStorePreferences.edit { prefs ->
            prefs[currentHouseholdIdPrefKey] = householdId
        }
    }
}
