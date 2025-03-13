package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.ui.home.model.CakeGraphUiModel.Slice
import com.section11.expenselens.ui.utils.formatToTwoDecimal

private const val HUNDRED = 100

fun HouseholdExpenses.getTotalExpensesValue(): Float {
    return expenses.sumOf { expense -> expense.total }.toFloat()
}

fun HouseholdExpenses.getSlicesByCategory(totalExpenses: Float): List<Slice> {
    return expenses.groupBy { it.category }
        .map { (category, value) ->
            val totalOfCurrentCategory = value.sumOf { it.total }.toFloat()
            Slice(
                "$category - ${calculatePercentage(totalOfCurrentCategory, totalExpenses)}%",
                totalOfCurrentCategory
            )
        }
}

private fun calculatePercentage(sliceValue: Float, total: Float): String {
    return ((sliceValue / total) * HUNDRED).formatToTwoDecimal()
}
