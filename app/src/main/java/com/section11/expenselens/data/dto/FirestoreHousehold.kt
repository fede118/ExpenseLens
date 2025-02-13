package com.section11.expenselens.data.dto

data class FirestoreHousehold(
    val id: String,
    val name: String,
    val users: List<String>
) {

    companion object {
        const val NAME_FIELD = "name"
        const val USERS_FIELD = "users"
    }
}
