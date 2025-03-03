package com.section11.expenselens.data.dto

data class FirestoreHousehold(
    val id: String,
    val name: String,
    val users: List<String>
)
