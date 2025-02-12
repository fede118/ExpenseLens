package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput
import java.util.Locale
import javax.inject.Inject

class ExpenseReviewScreenUiMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun mapExpenseInfoToUiModel(
        expenseInformation: ExpenseInformation?,
        extractedText: String?
    ): ExpenseReviewUiModel {
        val reviewRows = mutableListOf<ReviewRow>()
        reviewRows.addCategorySection(expenseInformation?.estimatedCategory)
        reviewRows.addTotalSection(expenseInformation?.total)

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
                id = categoryTitle,
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
                id = totalLabel,
                title = totalLabel,
                value = total.orEmpty().replace(dollarSing, String()),
                type = TextInput
            )
        )
    }
}
