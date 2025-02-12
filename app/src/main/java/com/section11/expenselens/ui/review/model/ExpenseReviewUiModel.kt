package com.section11.expenselens.ui.review.model

data class ExpenseReviewUiModel(
    val extractedText: String?, // This will be removed, its just for development
    val reviewRows: List<ReviewRow>
) {
    data class ReviewRow(
        val id: String,
        val title: String,
        val value: String,
        val type: ReviewRowType
    ) {
        sealed class ReviewRowType {
            data object TextInput : ReviewRowType()
            data class DropdownMenu(val options: List<String>) : ReviewRowType()
        }
    }
}
