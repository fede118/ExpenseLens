package com.section11.expenselens.ui.home.mapper

import androidx.compose.ui.graphics.Color
import com.section11.expenselens.R
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Accepted
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Rejected
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.HouseholdInviteResultEvent
import com.section11.expenselens.ui.home.model.InviteStatusUiModel
import com.section11.expenselens.ui.home.model.PendingInvitesUiModel
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

    private fun List<HouseholdInvite>?.toPendingInvitesUiModel(): List<PendingInvitesUiModel> {
        val pendingInvites = mutableListOf<PendingInvitesUiModel>()
        this?.map {
            pendingInvites.add(
                PendingInvitesUiModel(
                    id = it.householdId,
                    householdName = it.householdName,
                    timestamp = it.timestamp,
                    status = when(it.status) {
                        Pending -> InviteStatusUiModel.Pending
                        Accepted -> InviteStatusUiModel.Accepted
                        Rejected -> InviteStatusUiModel.Rejected
                    }
                )
            )
        }
        return pendingInvites
    }

    fun getSignOutSuccessMessage(): String {
        return resourceProvider.getString(R.string.home_screen_sign_out_success)
    }

    fun getHouseholdCreationErrorMessage(): String {
        return resourceProvider.getString(R.string.home_screen_household_creation_failure)
    }

    fun getHouseholdInviteResultEvent(exception: Throwable? = null): HouseholdInviteResultEvent {
        return if (exception != null) {
            HouseholdInviteResultEvent(
                getInvitationErrorMessage(exception),
                Color.Red
            )
        } else {
            HouseholdInviteResultEvent(
                getSuccessInvitationMessage(),
                Color.Green
            )
        }
    }

    private fun getInvitationErrorMessage(throwable: Throwable): String {
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

    private fun getSuccessInvitationMessage(): String {
        return resourceProvider.getString(R.string.home_screen_household_invite_success)
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
