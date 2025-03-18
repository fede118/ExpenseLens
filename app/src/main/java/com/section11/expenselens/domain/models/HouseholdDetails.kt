package com.section11.expenselens.domain.models

/**
 * Data class representing the details of a household.
 */
data class HouseholdDetails(
    var id: String,
    var name: String,
    var usersIds: List<String>
)

/**
 * Data class representing the details of a household with the emails of the users.
 *
 * I'm not sure I like this approach of having an object with ids to be mapped to the object with emails
 * see HouseholdDetailsUseCase. But the alternatives were:
 * - reusing the object above and re writing the "users" field to be a list of emails (before I renamed
 * it to usersIds)
 * - returning a Pair or something similar
 */
data class HouseholdDetailsWithUserEmails(
    var id: String,
    var name: String,
    var usersEmails: List<String>
)
