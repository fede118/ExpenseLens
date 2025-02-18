package com.section11.expenselens.data.constants

/**
 * Current Data structure:
 * - Documents:
 *      - users/{userId} (collection)
 *          - households (list of households)
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
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val USERS_FIELD = "users"
    }
}
