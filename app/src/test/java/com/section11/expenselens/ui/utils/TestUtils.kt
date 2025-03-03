package com.section11.expenselens.ui.utils

import com.section11.expenselens.domain.models.UserData

fun getUserData(
    id: String = "id",
    idToken: String = "idToken",
    name: String = "name",
    profilePic: String = "profilePic",
    email: String = "email",
    notificationToken: String = "notificationToken"
): UserData {
    return UserData(
        id,
        idToken,
        name,
        profilePic,
        email,
        notificationToken
    )
}
