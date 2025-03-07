package com.section11.expenselens.data.mapper

import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.models.Expense

fun FirestoreExpense.toDomainExpense() = Expense(
    category = category,
    total = total,
    date = timestamp.toDate(),
    userId = userId,
    userDisplayName = userDisplayName,
    note = note,
    distributedExpense = distributedExpense
)
