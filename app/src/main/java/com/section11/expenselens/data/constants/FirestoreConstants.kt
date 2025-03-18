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
 *          - notificationsToken
 *      - households/{householdId} (collection)
 *              - id
 *              - name
 *              - users
 *              - expenses
 *                  - expenseId
 *                  - category
 *                  - total
 *                  - timestamp
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
         * Not all fields are used right now. Add them when needed.
         */
        object HouseholdsCollection {
            const val USERS_FIELD = "users"
            const val EXPENSES_FIELD = "expenses"

            object ExpensesArray {
                const val TIMESTAMP_FIELD = "timestamp"
            }
        }
    }
}
