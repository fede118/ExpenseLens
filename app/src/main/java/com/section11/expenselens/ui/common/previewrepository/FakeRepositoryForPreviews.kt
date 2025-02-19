package com.section11.expenselens.ui.common.previewrepository

import android.content.Context
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.section11.expenselens.data.dto.FirestoreExpense
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import kotlin.random.Random

private const val LOREM_IPSUM_SIZE = 200
private val loremIpsum = LoremIpsum(LOREM_IPSUM_SIZE).values.first()

class FakeRepositoryForPreviews(context: Context) {

    private val resourceProvider = ResourceProviderImpl(context)

    fun getUserSignedInState(withHousehold: Boolean = true) = UserSignedIn(
        greeting = "Hello from fake repo",
        user = UserInfoUiModel(
            id = "id",
            displayName = "Test User",
            profilePic = ""
        ),
        householdInfo = if (withHousehold) {
            HouseholdUiState(
                id = "id",
                name = "Fake Repo household"
            )
        } else {
            null
        }
    )

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

    @Suppress("MagicNumber") // this is just for Previews
    fun getExpenseHistoryList(size: Int = 4): List<FirestoreExpense> {
        return List(size) {
            FirestoreExpense(
                category = "Category $it",
                total = Random.nextDouble(100.0, 1000.0),
                userDisplayName = "Name $it",
                note = if (it % 2 == 0) "Note $it" else ""
            )
        }
    }
}
