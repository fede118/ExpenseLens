package com.section11.expenselens.domain.models

data class UserData(
    val idToken: String,
    val id: String,
    val displayName: String?,
    val profilePic: String?,
    val email: String,
    val notificationToken: String,
    val currentHouseholdId: String? = null
)
