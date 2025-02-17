package com.section11.expenselens.ui.common.previewrepository

import android.content.Context
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel

private const val LOREM_IPSUM_SIZE = 200
private val loremIpsum = LoremIpsum(LOREM_IPSUM_SIZE).values.first()

class FakeRepositoryForPreviews(context: Context) {

    private val resourceProvider = ResourceProviderImpl(context)

    fun getExtractedText(): String = loremIpsum

    fun getExpenseInformation() = SuggestedExpenseInformation(
        total = 500.00,
        estimatedCategory = Category.ENTERTAINMENT,
        date = "15/03/2025"
    )

    fun getExpenseReviewUiModel(): ExpenseReviewUiModel {
        val expenseReviewMapper = ExpenseReviewScreenUiMapper(resourceProvider)
        return expenseReviewMapper.mapExpenseInfoToUiModel(getExpenseInformation(), loremIpsum)
    }
}
