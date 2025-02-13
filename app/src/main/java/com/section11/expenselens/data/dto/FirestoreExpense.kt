package com.section11.expenselens.data.dto

import com.google.firebase.Timestamp

class FirestoreExpense(
    val category: String,
    val total: Double,
    val date: Timestamp,
    val userId: String,
    val note: String? = null,
    val distributedExpense: Map<String, Double>? = null
)
