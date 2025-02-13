package com.section11.expenselens.domain.models

import java.util.Date

data class ConsolidatedExpenseInformation(
    val total: Double,
    val category: Category,
    val date: Date,
    val note: String? = null,
    val distributedExpense: Map<String, Double>? = null
)
