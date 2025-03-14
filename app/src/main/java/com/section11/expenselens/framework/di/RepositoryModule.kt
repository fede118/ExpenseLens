package com.section11.expenselens.framework.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.section11.expenselens.BuildConfig
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.repository.FirebaseAuthenticationRepository
import com.section11.expenselens.data.repository.FirestoreHouseholdInvitationRepository
import com.section11.expenselens.data.repository.FirestoreHouseholdRepository
import com.section11.expenselens.data.repository.FirestoreUsersCollectionRepository
import com.section11.expenselens.data.repository.GeminiAiRepository
import com.section11.expenselens.data.repository.GoogleUserSessionRepository
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.repository.AuthenticationRepository
import com.section11.expenselens.domain.repository.ExpenseInfoExtractorRepository
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import com.section11.expenselens.domain.repository.HouseholdRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideGeminiAiRepository(retrofit: Retrofit, gson: Gson): ExpenseInfoExtractorRepository {
        return GeminiAiRepository(
            retrofit.create(GeminiService::class.java),
            BuildConfig.GEMINI_API_KEY,
            GeminiResponseMapper(gson)
        )
    }

    @Suppress("LongParameterList")
    @Provides
    fun providesTokenRepository(
        dataStore: DataStore<Preferences>,
        @Named(TOKEN_PREFERENCES_KEY) tokenPrefKey: Preferences.Key<String>,
        @Named(USER_ID_KEY) userIdPrefKey: Preferences.Key<String>,
        @Named(DISPLAY_NAME_KEY) displayNamePrefKey: Preferences.Key<String>,
        @Named(PROFILE_PIC_KEY) profilePicPrefKey: Preferences.Key<String>,
        @Named(EMAIL_PREFERENCES_KEY) emailPreferenceKey: Preferences.Key<String>,
        @Named(NOTIFICATION_PREFERENCE_KEY) notificationTokenPrefKey: Preferences.Key<String>,
        @Named(CURRENT_HOUSEHOLD_ID_KEY) currentHouseholdIdKey: Preferences.Key<String>
    ): UserSessionRepository {
        return GoogleUserSessionRepository(
            dataStore,
            tokenPrefKey,
            userIdPrefKey,
            displayNamePrefKey,
            profilePicPrefKey,
            emailPreferenceKey,
            notificationTokenPrefKey,
            currentHouseholdIdKey
        )
    }

    @Provides
    fun provideFirestoreExpensesRepository(firestore: FirebaseFirestore): HouseholdRepository {
        return FirestoreHouseholdRepository(firestore)
    }

    @Provides
    fun providesFirestoreUsersHouseholdsRepository(
        firestore: FirebaseFirestore
    ): UsersCollectionRepository {
        return FirestoreUsersCollectionRepository(firestore)
    }

    @Provides
    fun providesFirestoreHouseholdInvitationRepository(
        firestore: FirebaseFirestore
    ): HouseholdInvitationRepository {
        return FirestoreHouseholdInvitationRepository(firestore)
    }

    @Provides
    fun providesAuthenticationRepository(
        firebaseAuth: FirebaseAuth,
        firebaseMessaging: FirebaseMessaging,
        googleTokenMapper: GoogleTokenMapper,
    ): AuthenticationRepository {
        return FirebaseAuthenticationRepository(
            firebaseAuth,
            firebaseMessaging,
            googleTokenMapper
        )
    }
}
