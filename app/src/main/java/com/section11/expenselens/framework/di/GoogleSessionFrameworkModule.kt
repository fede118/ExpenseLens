package com.section11.expenselens.framework.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.section11.expenselens.BuildConfig
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
    fun providesGoogleCredentialsRequest(googleIdOption: GetGoogleIdOption): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    @Provides
    fun providesGoogleCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }
}
