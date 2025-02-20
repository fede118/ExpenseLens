package com.section11.expenselens.framework.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.section11.expenselens.BuildConfig
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.repository.FirestoreExpensesRepository
import com.section11.expenselens.data.repository.FirestoreHouseholdInvitationRepository
import com.section11.expenselens.data.repository.FirestoreUsersCollectionRepository
import com.section11.expenselens.data.repository.GeminiAiRepository
import com.section11.expenselens.data.repository.GoogleUserSessionRepository
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.repository.ExpenseInfoExtractorRepository
import com.section11.expenselens.domain.repository.ExpensesRepository
import com.section11.expenselens.domain.repository.HouseholdInvitationRepository
import com.section11.expenselens.domain.repository.UsersCollectionRepository
import com.section11.expenselens.domain.repository.UserSessionRepository
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

    @Provides
    fun providesTokenRepository(
        dataStore: DataStore<Preferences>,
        @Named(TOKEN_PREFERENCES_KEY) tokenPrefKey: Preferences.Key<String>,
        @Named(USER_ID_KEY) userIdPrefKey: Preferences.Key<String>,
        @Named(DISPLAY_NAME_KEY) displayNamePrefKey: Preferences.Key<String>,
        @Named(PROFILE_PIC_KEY) profilePicPrefKey: Preferences.Key<String>
    ): UserSessionRepository {
        return GoogleUserSessionRepository(
            dataStore,
            tokenPrefKey,
            userIdPrefKey,
            displayNamePrefKey,
            profilePicPrefKey
        )
    }

    @Provides
    fun provideFirestoreExpensesRepository(firestore: FirebaseFirestore): ExpensesRepository {
        return FirestoreExpensesRepository(firestore)
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
}
