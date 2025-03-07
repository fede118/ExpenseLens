package com.section11.expenselens.ui.utils

import com.section11.expenselens.domain.models.UserData

@Suppress("LongParameterList") // this is just a test helper function to get mock userData across tests
fun getUserData(
    id: String = "id",
    idToken: String = "idToken",
    name: String = "name",
    profilePic: String = "profilePic",
    email: String = "email",
    notificationToken: String = "notificationToken",
    currentHouseHoldId: String? = "currentHouseHoldId"
): UserData {
    return UserData(
        id,
        idToken,
        name,
        profilePic,
        email,
        notificationToken,
        currentHouseHoldId
    )
}
