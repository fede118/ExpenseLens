package com.section11.expenselens.data.constants

/**
 * Current Data structure:
 * - Documents:
 *      - users/{userId} (collection)
 *          - email
 *          - households (list of households)
 *              - id
 *              - name
 *          - invitations (list of invitations)
 *              - householdId
 *              - householdName
 *              - inviterId
 *              - status (pending, accepted, rejected)
 *              - timestamp
 *      - households/{householdId} (collection)
 *              - id
 *              - name
 *              - users
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

        object UsersCollection {
            const val EMAIL_FIELD = "email"
            const val HOUSEHOLDS_FIELD = "households"
            const val INVITATIONS_FIELD = "invitations"
            const val NOTIFICATIONS_TOKEN_FIELD = "notificationsToken"

            object UsersHouseholdsArray {
                const val HOUSEHOLD_ID_FIELD = "id"
                const val HOUSEHOLD_NAME_FIELD = "name"
            }

            object UsersInvitationsArray {
                const val INVITER_ID = "inviterId"
                const val INVITE_STATUS = "status"
                const val INVITE_TIMESTAMP = "timestamp"
                const val INVITE_HOUSEHOLD_ID = "householdId"
                const val INVITE_HOUSEHOLD_NAME = "householdName"
            }
        }

        /**
         * Commented Fields are not being used right now. But will be used in the future. Thats how
         * the structure should be.
         */
        object HouseholdsCollection {
            const val USERS_FIELD = "users"
            const val EXPENSES_FIELD = "expenses"
//            const val ID_FIELD = "id"
//            const val NAME_FIELD = "name"

//            object ExpensesArray {
//                const val EXPENSE_ID_FIELD = "expenseId"
//                const val CATEGORY_FIELD = "category"
//                const val TOTAL_FIELD = "total"
//                const val DATE_FIELD = "date"
//                const val USER_ID_FIELD = "userId"
//                const val USER_DISPLAY_NAME_FIELD = "userDisplayName"
//                const val NOTE_FIELD = "note"
//            }
        }
    }
}
