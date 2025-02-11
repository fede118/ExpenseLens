package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import javax.inject.Inject

class HomeScreenUiMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

    fun getGreeting(): String = resourceProvider.getString(R.string.welcome_greeting)

    fun getUserData(userData: UserData): UserInfoUiModel {
        return UserInfoUiModel(
            id = userData.id,
            displayName = userData.displayName,
            profilePic = userData.profilePic
        )
    }

    fun getSignOutSuccessMessage(): String {
        return resourceProvider.getString(R.string.home_screen_sign_out_success)
    }
}
