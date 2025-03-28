package com.section11.expenselens.framework.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.section11.expenselens.BuildConfig
import com.section11.expenselens.framework.credentials.GoogleCredentialManager
import com.section11.expenselens.framework.utils.GoogleTokenMapper
import com.section11.expenselens.framework.utils.GoogleTokenMapperImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class GoogleSessionFrameworkModule {

    @Provides
    fun providesGoogleIdOption() : GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
//            .setNonce(<nonce string to use when generating a Google ID token>)
            .build()
    }

    @Provides
    fun providesGoogleCredentialManager(
        @ApplicationContext context: Context,
        googleIdOption: GetGoogleIdOption
    ): GoogleCredentialManager {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        return GoogleCredentialManager(CredentialManager.create(context), request)
    }

    @Provides
    fun provideGoogleTokenMapper(): GoogleTokenMapper {
        return GoogleTokenMapperImpl()
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}
