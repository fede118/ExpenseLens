package com.section11.expenselens.framework.credentials

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import javax.inject.Inject

class GoogleCredentialManager @Inject constructor(
    private val credentialManager: CredentialManager,
    private val credentialsRequest: GetCredentialRequest
) {

    suspend fun getCredentials(context: Context): GetCredentialResponse {
        return credentialManager.getCredential(context, credentialsRequest)
    }
}
