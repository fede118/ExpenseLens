package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.framework.utils.toDate
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.ADD_NOTE
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.DATE_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ReviewRowType
import com.section11.expenselens.ui.review.model.ReviewRowType.DatePickerType
import com.section11.expenselens.ui.review.model.ReviewRowType.DropdownMenuType
import com.section11.expenselens.ui.review.model.ReviewRowType.MoneyInputType
import com.section11.expenselens.ui.review.model.ReviewRowType.TextInputType
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExpenseReviewScreenUiMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun mapExpenseInfoToUiModel(
        suggestedExpenseInformation: SuggestedExpenseInformation?,
        extractedText: String?
    ): ExpenseReviewUiModel {
        val reviewRows = mutableListOf<ReviewRow>()
        ExpenseReviewSections.entries.forEach { section ->
            reviewRows.addSection(section, suggestedExpenseInformation)
        }

        return ExpenseReviewUiModel(
            extractedText = extractedText,
            reviewRows = reviewRows
        )
    }

    private fun MutableList<ReviewRow>.addSection(
        section: ExpenseReviewSections,
        suggestedExpenseInformation: SuggestedExpenseInformation?
    ) {
        add(
            ReviewRow(
                section = section,
                title = section.getSectionTitle(),
                value = section.getSectionValue(suggestedExpenseInformation),
            )
        )
    }

    private fun ExpenseReviewSections.getSectionTitle(): String {
        return when(this) {
            CATEGORY_SELECTION -> getString(R.string.expense_review_screen_category_title)
            DATE_SELECTION -> getString(R.string.date_picker_title)
            TOTAL -> getString(R.string.expense_review_screen_total_title)
            ADD_NOTE -> getString(R.string.expense_review_screen_note_title)
        }
    }

    private fun ExpenseReviewSections.getSectionValue(
        expenseInfo: SuggestedExpenseInformation?
    ): String {
        return when(this) {
            CATEGORY_SELECTION -> getCategorySectionValue(expenseInfo)
            DATE_SELECTION -> expenseInfo?.date ?: getString(R.string.expense_review_screen_no_date)
            TOTAL -> getTotalValue(expenseInfo)
            ADD_NOTE -> String()
        }
    }

    private fun getCategorySectionValue(expenseInfo: SuggestedExpenseInformation?): String {
        val estimatedCategoryName = expenseInfo?.estimatedCategory?.displayName
        val categoriesDisplayNameList = Category.entries.map { it.displayName }

        val preselectedCategory: String = estimatedCategoryName?.let {
            if (categoriesDisplayNameList.contains(it)) {
                it.lowercase().replaceFirstChar { char -> char.capitalize() }
            } else {
                null
            }
        } ?: getString(R.string.expense_review_screen_select_category)

        return preselectedCategory
    }

    /**
     * The service returns the total as a string with the dollar sign.
     * But we want to show the $ icon in the UI. So we remove it. Maybe we should have the service
     * return a double with just the value
     */
    private fun getTotalValue(expenseInfo: SuggestedExpenseInformation?): String {
        val dollarSing = getString(R.string.dollar_sign)
        return expenseInfo?.total.orEmpty().replace(dollarSing, String())
    }

    private fun getString(id: Int) = resourceProvider.getString(id)

    private fun Char.capitalize() = titlecase(Locale.getDefault())

    fun toConsolidatedExpense(event: ExpenseSubmitted): ConsolidatedExpenseInformation {
        // TODO find a better way to do this. This shouldn't be null at this point
        // none of this fields. I need a validator class before submitting the expense
        var total = 0.00
        var category: Category = Category.MISCELLANEOUS
        var date = Date()
        var note = String()
        event.expenseReviewUiModel.reviewRows.forEach { row ->
            when (row.section) {
                CATEGORY_SELECTION -> Category.fromDisplayName(row.value)?.let { category = it }
                DATE_SELECTION -> {
                    row.value.let {
                        date = it.toDate() ?: throw IllegalArgumentException("Invalid date format")
                    }
                }
                TOTAL -> row.value.currencyStringToDouble()?.let { total = it }
                ADD_NOTE -> row.value.let { note = it }
            }
        }

        return ConsolidatedExpenseInformation(
            total = total,
            category = category,
            date = date,
            note = note
        )
    }

    private fun String.currencyStringToDouble(): Double? {
        return this.replace(Regex("[^0-9.-]"), "").toDoubleOrNull()
    }

    /**
     * This enum class represents each section of the review screen.
     *
     * So currently wwe show:
     * CATEGORY,
     * DATE_PICKER,
     * TOTAL
     * NOTE
     *
     * Basically its the structure of the screen.
     *
     * Each section has its view type defined. This way when adding a new section you HAVE TO set a
     * view type, so that the screen knows how to show it
     */
    enum class ExpenseReviewSections(val viewType: ReviewRowType) {
        CATEGORY_SELECTION(DropdownMenuType(Category.entries.map { it.displayName })),
        DATE_SELECTION(DatePickerType),
        TOTAL(MoneyInputType),
        ADD_NOTE(TextInputType)
    }
}
