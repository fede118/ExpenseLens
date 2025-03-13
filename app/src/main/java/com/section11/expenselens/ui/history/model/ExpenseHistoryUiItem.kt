package com.section11.expenselens.ui.history.model

data class ExpenseHistoryUiItem(
    val expenseId: String,
    val category: String,
    val total: Double,
    val date: String,
    val userId: String,
    val userDisplayName: String?,
    val note: String?,
    val distributedExpense: Map<String, Double>? = null
)
