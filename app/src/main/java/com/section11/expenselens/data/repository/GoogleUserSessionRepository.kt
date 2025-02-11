package com.section11.expenselens.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GoogleUserSessionRepository @Inject constructor(
    private val dataStorePreferences: DataStore<Preferences>,
    private val tokenPrefKey: Preferences.Key<String>,
    private val userIdPrefKey: Preferences.Key<String>,
    private val displayNamePrefKey: Preferences.Key<String>,
    private val profilePicPrefKey: Preferences.Key<String>
) : UserSessionRepository {

    override suspend fun saveUser(idToken: String, id: String, name: String?, profilePic: Uri?) {
        dataStorePreferences.edit { prefs ->
            prefs[tokenPrefKey] = idToken
            prefs[userIdPrefKey] = id
            name?.let { prefs[displayNamePrefKey] = it }
            profilePic?.let { prefs[profilePicPrefKey] = it.toString() }
        }
    }

    override suspend fun getUser(): UserData? {
        val prefs = dataStorePreferences.data.first()
        val idToken = prefs[tokenPrefKey]
        val id = prefs[userIdPrefKey]
        val displayName = prefs[displayNamePrefKey]
        val profilePic = prefs[profilePicPrefKey]
        return  if (idToken.isNullOrEmpty() || id.isNullOrEmpty()) {
            null
        } else {
            UserData(
                idToken = idToken,
                id = id,
                displayName = displayName,
                profilePic = profilePic
            )
        }
    }

    override suspend fun clearUser() {
        dataStorePreferences.edit { it.clear() }
    }
}
