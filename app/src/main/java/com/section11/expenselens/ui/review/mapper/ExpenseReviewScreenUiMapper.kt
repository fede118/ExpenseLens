package com.section11.expenselens.ui.review.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProvider
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
import com.section11.expenselens.ui.review.validator.InvalidExpenseCategoryException
import com.section11.expenselens.ui.review.validator.InvalidExpenseDateException
import com.section11.expenselens.ui.review.validator.InvalidExpenseTotalException
import java.util.Locale
import javax.inject.Inject

class ExpenseReviewScreenUiMapper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun mapExpenseInfoToUiModel(
        suggestedExpenseInformation: SuggestedExpenseInformation,
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

    fun getEmptyExpenseReviewUiModel(): ExpenseReviewUiModel {
        val emptySuggestedExpenseInformation = SuggestedExpenseInformation(
            date = null,
            total = 0.00,
            estimatedCategory = null,
        )
        return mapExpenseInfoToUiModel(emptySuggestedExpenseInformation, null)
    }

    private fun MutableList<ReviewRow>.addSection(
        section: ExpenseReviewSections,
        suggestedExpenseInformation: SuggestedExpenseInformation
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
        expenseInfo: SuggestedExpenseInformation
    ): String {
        return when(this) {
            CATEGORY_SELECTION -> getCategorySectionValue(expenseInfo)
            DATE_SELECTION -> expenseInfo.date ?: getString(R.string.expense_review_screen_no_date)
            TOTAL -> expenseInfo.total.toString()
            ADD_NOTE -> String()
        }
    }

    private fun getCategorySectionValue(expenseInfo: SuggestedExpenseInformation): String {
        val estimatedCategoryName = expenseInfo.estimatedCategory?.displayName
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

    private fun getString(id: Int) = resourceProvider.getString(id)

    private fun Char.capitalize() = titlecase(Locale.getDefault())

    fun getNoExpenseFoundMessageAndUiModel(): Pair<String, ExpenseReviewUiModel> {
        val message = getString(R.string.expense_review_screen_no_expense_found)
        val emptySuggestedExpenseInformation = SuggestedExpenseInformation(
            date = null,
            total = 0.00,
            estimatedCategory = null,
        )
        val uiModel = mapExpenseInfoToUiModel(emptySuggestedExpenseInformation, null)
        return message to uiModel
    }

    fun getErrorMessageFromExpenseValidationException(exception: Throwable): String {
        return when(exception) {
            is InvalidExpenseCategoryException -> getString(R.string.expense_review_screen_error_in_category)
            is InvalidExpenseDateException -> getString(R.string.expense_review_screen_error_in_date)
            is InvalidExpenseTotalException -> getString(R.string.expense_review_screen_error_in_total)
            else -> getString(R.string.expense_review_screen_error_when_submitting)
        }
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
