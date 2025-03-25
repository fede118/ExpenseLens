package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.model.CakeGraphUiModel
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import javax.inject.Inject

class HomeScreenUiMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

    fun getUserSignInModel(
        userData: UserData,
        userHousehold: HouseholdExpenses?,
        pendingInvites: List<HouseholdInvite>?
    ): UserSignedIn {
        return UserSignedIn(
            UserInfoUiModel(
                id = userData.id,
                displayName = userData.displayName,
                profilePic = userData.profilePic,
                pendingInvites = pendingInvites.toPendingInvitesUiModel()
            ),
            userHousehold?.run {
                val total = userHousehold.getTotalExpensesValue()
                HouseholdUiState(
                    householdInfo.id,
                    householdInfo.name,
                    CakeGraphUiModel(
                        slices = getSlicesByCategory(total),
                        chartCenterText = resourceProvider
                            .getString(R.string.cake_graph_current_month_expenses, total)
                    )
                )
            }
        )
    }

    fun getSignOutSuccessMessage(): String {
        return resourceProvider.getString(R.string.home_screen_sign_out_success)
    }

    fun getHouseholdCreationErrorMessage(): String {
        return resourceProvider.getString(R.string.home_screen_household_creation_failure)
    }

    /**
     * When household has been just created then we know there is now graph info, so we are not
     * retrieving it and thus passing null to the [HouseholdUiState]
     *
     * This method should only be called when a household has just been created
     */
    fun updateSignedInUiWhenHouseholdCreated(
        userSignedIn: UserSignedIn,
        household: UserHousehold
    ): UserSignedIn {
        return userSignedIn.copy(
            householdInfo = HouseholdUiState(
                household.id,
                household.name,
                null
            )
        )
    }

    fun getGenericErrorMessage(): String {
        return resourceProvider.getString(R.string.generic_error_message)
    }
}
