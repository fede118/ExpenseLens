package com.section11.expenselens.data.dto

import com.google.firebase.Timestamp

/**
 * Firestore requires a no argument constructor
 */
data class FirestoreExpense(
    val category: String = "",
    val total: Double = 0.00,
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String = "",
    val userDisplayName: String? = "",
    val note: String? = null,
    val distributedExpense: Map<String, Double>? = null
)
