package com.section11.expenselens.domain.models

data class UserHousehold(
    val id: String,
    val name: String
)

data class HouseholdExpenses(
    val householdInfo: UserHousehold,
    val expenses: List<Expense>
)
