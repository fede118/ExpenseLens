package com.section11.expenselens.ui.household.model

/**
 * UI model for household details screen
 *
 * @param userId id of the current user
 * @param householdId id of the household
 * @param householdName name of the household
 * @param users list of users in the household
 * @param cta a call to action that can be either Leave or Delete button defined on the amount of
 * users in the household. If 1 user is present, the label will be "Delete" (since its the only user
 * in the household), else it will be "Leave" (since there are other users in the household)
 */
data class HouseholdDetailsUiModel(
    val userId: String,
    val householdId: String,
    val householdName: String,
    val users: List<String>,
    val cta: HouseholdDetailsCta
) {
    sealed class HouseholdDetailsCta(open val label: String) {
        data class Delete(override val label : String) : HouseholdDetailsCta(label)
        data class Leave(override val label : String) : HouseholdDetailsCta(label)
    }
}
