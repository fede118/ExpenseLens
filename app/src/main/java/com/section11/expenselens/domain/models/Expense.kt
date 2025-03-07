package com.section11.expenselens.domain.models

import java.util.Date

data class Expense(
    val category: String,
    val total: Double,
    val date: Date,
    val userId: String,
    val userDisplayName: String?,
    val note: String?,
    val distributedExpense: Map<String, Double>? = null
)
