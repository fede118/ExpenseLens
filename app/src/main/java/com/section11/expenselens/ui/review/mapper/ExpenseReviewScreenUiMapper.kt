package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TOTAL_ID = "total"
private const val CATEGORY_ID = "category"

class ExpenseReviewScreenUiMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun mapExpenseInfoToUiModel(
        suggestedExpenseInformation: SuggestedExpenseInformation?,
        extractedText: String?
    ): ExpenseReviewUiModel {
        val reviewRows = mutableListOf<ReviewRow>()
        reviewRows.addCategorySection(suggestedExpenseInformation?.estimatedCategory)
        reviewRows.addTotalSection(suggestedExpenseInformation?.total)

        return ExpenseReviewUiModel(
            extractedText = extractedText,
            reviewRows = reviewRows
        )
    }

    private fun MutableList<ReviewRow>.addCategorySection(estimatedCategory: Category?) {
        val categoryTitle = getString(R.string.expense_review_screen_category_title)
        val categories = Category.entries.map { it.displayName }
        val estimatedCategoryName = estimatedCategory?.displayName

        val preselectedCategory: String = estimatedCategoryName?.let {
            if (categories.contains(it)) {
                it.lowercase().replaceFirstChar { char -> char.capitalize() }
            } else {
                null
            }
        } ?: getString(R.string.expense_review_screen_select_category)
        add(
            ReviewRow(
                id = CATEGORY_ID,
                title = categoryTitle,
                value = preselectedCategory,
                type = DropdownMenu(categories)
            )
        )
    }

    private fun getString(id: Int) = resourceProvider.getString(id)

    private fun Char.capitalize() = titlecase(Locale.getDefault())

    private fun MutableList<ReviewRow>.addTotalSection(total: String?) {
        val totalLabel = getString(R.string.expense_review_screen_total_title)
        val dollarSing = getString(R.string.dollar_sign)
        add(
            ReviewRow(
                id = TOTAL_ID,
                title = totalLabel,
                value = total.orEmpty().replace(dollarSing, String()),
                type = TextInput
            )
        )
    }

    fun toConsolidatedExpense(event: ExpenseSubmitted): ConsolidatedExpenseInformation {
        // todo find a better way to do this. This shouldn't be null at this point
        // none of this fields. I need a validator class before submitting the expense
        var total = 0.00
        var category: Category = Category.MISCELLANEOUS
        event.expenseReviewUiModel.reviewRows.forEach { row ->
            when (row.id) {
                TOTAL_ID -> row.value.currencyStringToDouble()?.let { total = it }
                CATEGORY_ID -> Category.fromDisplayName(row.value)?.let { category = it }
            }
        }

        return ConsolidatedExpenseInformation(
            total = total,
            category = category,
            date = Date(),
            note = "test note",
        )
    }

    private fun String.currencyStringToDouble(): Double? {
        return this.replace(Regex("[^0-9.-]"), "").toDoubleOrNull()
    }
}
