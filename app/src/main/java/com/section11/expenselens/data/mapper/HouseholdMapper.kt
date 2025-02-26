package com.section11.expenselens.data.mapper

import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_ID_FIELD
import com.section11.expenselens.data.constants.FirestoreConstants.Collections.UsersCollection.UsersHouseholdsArray.HOUSEHOLD_NAME_FIELD
import com.section11.expenselens.domain.models.UserHousehold

fun List<*>.mapToHouseholdsList(): List<UserHousehold> {
    return mapNotNull {
        val map = it as? Map<*, *>
        val id = map?.get(HOUSEHOLD_ID_FIELD) as? String
        val name = map?.get(HOUSEHOLD_NAME_FIELD) as? String
        if (id != null && name != null) UserHousehold(id, name) else null
    }
}
