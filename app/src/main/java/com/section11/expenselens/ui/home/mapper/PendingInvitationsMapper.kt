package com.section11.expenselens.ui.home.mapper

import androidx.compose.ui.graphics.Color
import com.section11.expenselens.R
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Accepted
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Rejected
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.HouseholdInviteResultEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.model.CakeGraphUiModel
import com.section11.expenselens.ui.home.model.InviteStatusUiModel
import com.section11.expenselens.ui.home.model.PendingInvitesUiModel
import com.section11.expenselens.ui.utils.UiState
import javax.inject.Inject

class PendingInvitationsMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

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

    fun updateInvitesAndHousehold(
        userSignedInState: UserSignedIn,
        newPendingInvites: List<HouseholdInvite>?,
        household: HouseholdExpenses?
    ): UserSignedIn {
        return userSignedInState.copy(
            householdInfo = household?.let {
                val totalExpenses = it.getTotalExpensesValue()
                HouseholdUiState(
                    it.householdInfo.id,
                    it.householdInfo.name,
                    CakeGraphUiModel(
                        slices = it.getSlicesByCategory(totalExpenses),
                        chartCenterText = "Monthly Expenses: \n \$${totalExpenses}"
                    )
                )
            },
            user = userSignedInState.user.copy(
                pendingInvites = newPendingInvites.toPendingInvitesUiModel()
            )
        )
    }

    fun setPendingInviteLoading(uiState: UiState, inviteTap: HouseholdInviteTap): UiState {
        return if (uiState is UserSignedIn) {
            val indexOfLoadingItem = uiState.user.pendingInvites.indexOfFirst { invitesUiModel ->
                invitesUiModel.householdId == inviteTap.householdId
            }
            val pendingInvites = uiState.user.pendingInvites.toMutableList()
            pendingInvites[indexOfLoadingItem] =
                pendingInvites[indexOfLoadingItem].copy(isLoading = true)
            uiState.copy(user = uiState.user.copy(pendingInvites = pendingInvites))
        } else {
            uiState
        }
    }
}

fun List<HouseholdInvite>?.toPendingInvitesUiModel(): List<PendingInvitesUiModel> {
    val pendingInvites = mutableListOf<PendingInvitesUiModel>()
    this?.map {
        pendingInvites.add(
            PendingInvitesUiModel(
                inviteId = it.inviteId,
                householdId = it.householdId,
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
