package com.section11.expenselens.framework.utils

import android.os.Bundle
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * This is a wrapper class of GoogleIdTokenCredential.createFrom because it is not possible to mock it
 * even with mockk static framework is giving me errors. So I decided to stop wasting time and just
 * create a wrapper and inject it
 */
interface GoogleTokenMapper {
    fun toGoogleToken(bundle: Bundle): GoogleIdTokenCredential
}

class GoogleTokenMapperImpl : GoogleTokenMapper {

    override fun toGoogleToken(bundle: Bundle): GoogleIdTokenCredential {
        return GoogleIdTokenCredential.createFrom(bundle)
    }
}
