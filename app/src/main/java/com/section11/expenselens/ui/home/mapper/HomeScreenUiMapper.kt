package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import javax.inject.Inject

class HomeScreenUiMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

    fun getGreeting(): String = resourceProvider.getString(R.string.welcome_greeting)

    fun getUserSignInModel(
        userData: UserData,
        userHousehold: UserHousehold?,
        pendingInvites: List<HouseholdInvite>?
    ): UserSignedIn {
        return UserSignedIn(
            getGreeting(),
            UserInfoUiModel(
                id = userData.id,
                displayName = userData.displayName,
                profilePic = userData.profilePic,
                pendingInvites = pendingInvites.toPendingInvitesUiModel()
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

    fun updateSignedInUiWithHousehold(
        userSignedIn: UserSignedIn,
        household: UserHousehold
    ): UserSignedIn {
        return userSignedIn.copy(
            householdInfo = HouseholdUiState(
                household.id,
                household.name
            )
        )
    }
}
