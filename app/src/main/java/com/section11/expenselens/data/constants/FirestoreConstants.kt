package com.section11.expenselens.data.constants

/**
 * Current Data structure:
 * - Documents:
 *      - users/{userId} (collection)
 *          - email
 *          - households (list of households)
 *          - invitations (list of invitations)
 *              - householdId
 *              - householdName
 *              - inviterId
 *              - status (pending, accepted, rejected)
 *      - households/{householdId} (collection)
 *              - expenses
 *                  - expenseId
 *                  - category
 *                  - total
 *                  - date
 *                  - userId
 *                  - userDisplayName
 *                  - note
 *                  - distributedExpense
 *
 */
object FirestoreConstants {
    object Collections {
        const val USERS_COLLECTION = "users"
        const val HOUSEHOLDS_COLLECTION = "households"
        const val EXPENSES_COLLECTION = "expenses"
    }

    object Fields {
        const val HOUSEHOLDS_FIELD = "households"  // Field inside `users/{userId}`
        const val HOUSEHOLD_ID_FIELD = "householdId"
        const val HOUSEHOLD_NAME_FIELD = "householdName"
        const val INVITATIONS_FIELD = "invitations"
        const val INVITER_ID_FIELD = "inviterId"
        const val INVITE_STATUS_FIELD = "status"
        const val INVITE_TIMESTAMP_FIELD = "timestamp"
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val EMAIL_FIELD = "email"
    }
}
