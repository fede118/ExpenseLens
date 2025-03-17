package com.section11.expenselens.ui.household

import com.section11.expenselens.R
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave

class HouseholdDetailsUiMapper(private val resourceProvider: ResourceProvider) {

    fun getHouseholdDetailsUiModel(householdName: String, usersIds: List<String>): HouseholdDetailsUiModel {
        return HouseholdDetailsUiModel(
            householdName = householdName,
            users = usersIds,
            cta = getLeaverOrDeleteCta(usersIds)
        )
    }

    private fun getLeaverOrDeleteCta(users: List<String>): HouseholdDetailsCta {
        return if (users.size <= 1) {
            Delete(resourceProvider.getString(R.string.household_details_delete_household_label))
        } else {
            Leave(resourceProvider.getString(R.string.household_details_leave_household_label))
        }
    }
}
