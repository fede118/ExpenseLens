package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.ui.home.model.CakeGraphUiModel.Slice
import java.util.Locale

private const val HUNDRED = 100
private const val PERCENTAGE_FORMAT = "%.2f"

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
    return String.format(Locale.getDefault(), PERCENTAGE_FORMAT, (sliceValue / total) * HUNDRED)
}
