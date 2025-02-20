package com.section11.expenselens.ui.home.mapper

import androidx.compose.ui.graphics.Color
import com.section11.expenselens.R
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import javax.inject.Inject

class HomeScreenUiMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

    fun getGreeting(): String = resourceProvider.getString(R.string.welcome_greeting)

    fun getUserSignInModel(userData: UserData, userHousehold: UserHousehold?): UserSignedIn {
        return UserSignedIn(
            getGreeting(),
            UserInfoUiModel(
                id = userData.id,
                displayName = userData.displayName,
                profilePic = userData.profilePic
            ),
            userHousehold?.let {
                HouseholdUiState(it.id, it.name)
            }
        )
    }

    fun getSignOutSuccessMessage(): String {
        return resourceProvider.getString(R.string.home_screen_sign_out_success)
    }

    fun getHouseholdCreationErrorMessage(): String {
        return resourceProvider.getString(R.string.home_screen_household_creation_failure)
    }

    fun getInvitationErrorMessage(throwable: Throwable): String {
        return when(throwable) {
            is UserNotFoundException -> {
                resourceProvider.getString(R.string.home_screen_household_invite_user_not_found)
            }
            else -> getGenericInviteError()
        }
    }

    private fun getGenericInviteError(): String {
        return resourceProvider.getString(R.string.home_screen_household_invite_failure)
    }

    fun getSuccessInvitationMessage(): String {
        return resourceProvider.getString(R.string.home_screen_household_invite_success)
    }

    fun getInvitationErrorMessageColor() = Color.Red

    fun getInvitationSuccessMessageColor() = Color.Green
}
