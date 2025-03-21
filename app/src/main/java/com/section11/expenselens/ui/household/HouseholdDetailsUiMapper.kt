package com.section11.expenselens.ui.household

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave

class HouseholdDetailsUiMapper(private val resourceProvider: ResourceProvider) {

    fun getHouseholdDetailsUiModel(
        householdDetails: HouseholdDetailsWithUserEmails
    ): HouseholdDetailsUiModel {
        return with(householdDetails) {
            HouseholdDetailsUiModel(
                userId = currentUserId,
                householdId = householdId,
                householdName = name,
                users = usersEmails,
                cta = getLeaverOrDeleteCta(usersEmails)
            )
        }
    }

    private fun getLeaverOrDeleteCta(users: List<String>): HouseholdDetailsCta {
        return if (users.size <= 1) {
            Delete(resourceProvider.getString(R.string.household_details_delete_household_label))
        } else {
            Leave(resourceProvider.getString(R.string.household_details_leave_household_label))
        }
    }

    fun getNoHouseholdIdError(): String {
        return resourceProvider.getString(R.string.household_details_no_household_id_error)
    }

    fun getLeaveHouseholdSuccessMessage(householdName: String): String {
        return resourceProvider.getString(
            R.string.household_details_leave_household_success,
            householdName
        )
    }

    fun getLeaveHouseholdErrorMessage(): String {
        return resourceProvider.getString(R.string.generic_error_message)
    }

    fun getHouseholdDeletedSuccessMessage(householdName: String): String {
        return resourceProvider.getString(
            R.string.household_details_delete_household_success,
            householdName
        )
    }
}
