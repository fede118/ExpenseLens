package com.section11.expenselens.ui.review.model

import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections

data class ExpenseReviewUiModel(
    val extractedText: String?, // This will be removed, its just for development
    val reviewRows: List<ReviewRow>
) {
    data class ReviewRow(
        val section: ExpenseReviewSections,
        val title: String,
        val value: String
    )
}

/**
 * The types of views supported in the review screen. This is mapped to a type of composable in the
 * ExpenseReviewScreen
 *
 * For ex.:
 * MoneyInput will be shown as an TextField with a dollar sign and only numbers to be input
 * TextInput will be shown as a TextField
 * DatePicker will show the Material3 date picker\
 * DropdownMenu will show a drop down menu with the provided options
 */
sealed class ReviewRowType {
    data object MoneyInputType : ReviewRowType()
    data object TextInputType : ReviewRowType()
    data object DatePickerType : ReviewRowType()
    data class DropdownMenuType(val options: List<String>) : ReviewRowType()
}
