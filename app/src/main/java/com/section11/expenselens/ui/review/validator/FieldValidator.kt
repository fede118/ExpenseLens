package com.section11.expenselens.ui.review.validator

import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ConsolidatedExpenseInformation
import com.section11.expenselens.framework.utils.toDate
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.ADD_NOTE
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.CATEGORY_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.DATE_SELECTION
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections.TOTAL
import java.text.ParseException
import java.util.Date

class ExpenseValidator {

    fun validateExpense(submittedExpense: ExpenseSubmitted): Result<ConsolidatedExpenseInformation> {
        val reviewMap = submittedExpense.expenseReviewUiModel.reviewRows
            .associateBy { it.section }

        return try {
            Result.success(
                ConsolidatedExpenseInformation(
                    total = validateTotal(reviewMap[TOTAL]?.value),
                    category = validateCategory(reviewMap[CATEGORY_SELECTION]?.value),
                    date = validateDate(reviewMap[DATE_SELECTION]?.value),
                    note = reviewMap[ADD_NOTE]?.value.orEmpty()
                )
            )
        } catch (exception: ExpenseValidationException) {
            Result.failure(exception)
        }
    }

    private fun validateTotal(total: String?): Double =
        total?.toDoubleOrNull() ?: throw InvalidExpenseTotalException()

    private fun validateCategory(category: String?): Category =
        Category.fromDisplayName(category.orEmpty()) ?: throw InvalidExpenseCategoryException()

    private fun validateDate(date: String?): Date {
        return try {
            date?.toDate() ?: throw InvalidExpenseDateException()
        } catch (exception: ParseException) {
            throw InvalidExpenseDateException()
        }
    }
}
