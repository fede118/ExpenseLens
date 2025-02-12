package com.section11.expenselens.ui.common.previewrepository

import android.content.Context
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.section11.expenselens.R
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.DropdownMenu
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow.ReviewRowType.TextInput

private const val LOREM_IPSUM_SIZE = 200
private val loremIpsum = LoremIpsum(LOREM_IPSUM_SIZE).values.first()

class FakeRepositoryForPreviews(context: Context) {

    private val resourceProvider = ResourceProviderImpl(context)

    fun getExtractedText(): String = loremIpsum

    fun getExpenseInformation() = ExpenseInformation(
        total = "$500.00",
        estimatedCategory = Category.ENTERTAINMENT
    )

    fun getExpenseReviewUiModel(): ExpenseReviewUiModel {
        val reviewRows = mutableListOf<ExpenseReviewUiModel.ReviewRow>()
        val categoryLabel = resourceProvider.getString(R.string.expense_review_screen_category_title)
        reviewRows.add(
            ExpenseReviewUiModel.ReviewRow(
                id = categoryLabel,
                title = categoryLabel,
                value = Category.ENTERTAINMENT.displayName,
                type = DropdownMenu(Category.entries.map { it.displayName }))
        )


        val totalLabel = resourceProvider.getString(R.string.expense_review_screen_total_title)
        reviewRows.add(
            ExpenseReviewUiModel.ReviewRow(
                id = totalLabel,
                title = totalLabel,
                value = "$123.23",
                type = TextInput
            )
        )


        return ExpenseReviewUiModel(
            extractedText = loremIpsum,
            reviewRows = reviewRows
        )
    }
}
