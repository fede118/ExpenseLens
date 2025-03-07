package com.section11.expenselens.framework.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
const val USER_ID_KEY  = "user_id"
const val DISPLAY_NAME_KEY = "display_name"
const val TOKEN_PREFERENCES_KEY = "user_token"
const val PROFILE_PIC_KEY = "profile_pic"
const val NOTIFICATION_PREFERENCE_KEY = "notification_token_key"
const val EMAIL_PREFERENCES_KEY = "email_key"
const val CURRENT_HOUSEHOLD_ID_KEY = "current_household_id"

@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {

    private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

    @Provides
    @Singleton
    fun providesDataStorePreferences(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Named(TOKEN_PREFERENCES_KEY)
    fun providesTokenPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(TOKEN_PREFERENCES_KEY)
    }

    @Provides
    @Named(USER_ID_KEY)
    fun providesUserIdPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(USER_ID_KEY)
    }

    @Provides
    @Named(DISPLAY_NAME_KEY)
    fun providesDisplayNamePreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(DISPLAY_NAME_KEY)
    }

    @Provides
    @Named(PROFILE_PIC_KEY)
    fun providesProfilePicPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(PROFILE_PIC_KEY)
    }

    @Provides
    @Named(NOTIFICATION_PREFERENCE_KEY)
    fun providesNotificationTokenPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(NOTIFICATION_PREFERENCE_KEY)
    }

    @Provides
    @Named(EMAIL_PREFERENCES_KEY)
    fun providesEmailPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(EMAIL_PREFERENCES_KEY)
    }

    @Provides
    @Named(CURRENT_HOUSEHOLD_ID_KEY)
    fun providesCurrentHouseholdIdPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(CURRENT_HOUSEHOLD_ID_KEY)
    }
}
