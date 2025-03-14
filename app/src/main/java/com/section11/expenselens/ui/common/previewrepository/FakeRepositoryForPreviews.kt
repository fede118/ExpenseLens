package com.section11.expenselens.ui.common.previewrepository

import android.content.Context
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.google.firebase.Timestamp
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import java.util.Date
import kotlin.random.Random

private const val LOREM_IPSUM_SIZE = 200
private val loremIpsum = LoremIpsum(LOREM_IPSUM_SIZE).values.first()

class FakeRepositoryForPreviews(context: Context) {

    private val resourceProvider = ResourceProviderImpl(context)
    private val mapper = HomeScreenUiMapper(resourceProvider)

    fun getUserSignedInState(withHousehold: Boolean = true): UserSignedIn {
        return mapper.getUserSignInModel(
            userData = UserData(
                id = "id",
                idToken = "idToken",
                displayName = "Test User",
                profilePic = "",
                notificationToken = "",
                email = "test@email.com",
                currentHouseholdId = if (withHousehold) "id" else null
            ),
            userHousehold = if (withHousehold) {
                HouseholdExpenses(
                    householdInfo = UserHousehold("id", "Fake Repo household"),
                    expenses = getExpenseHistoryList()
                )
            } else {
                null
            },
            pendingInvites = listOf(
                HouseholdInvite(
                    householdId = "id",
                    status = Pending,
                    timestamp = Timestamp.now(),
                    inviteId = "inviteId",
                    householdName = "some household",
                    inviterId = "other userId"
                )
            )

        )
    }

    @Suppress("MagicNumber") // this is just for Previews
    private fun getExpenseHistoryList(): List<Expense> {
        return List(4) {
            Expense(
                category = "Category $it",
                total = Random.nextDouble(100.0, 1000.0),
                userDisplayName = "Name $it",
                note = if (it % 2 == 0) "Note $it" else "",
                date = Date(),
                userId = "userId",
            )
        }
    }

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
    fun getExpenseHistoryList(size: Int = 4): List<Expense> {
        return List(size) {
            Expense(
                category = "Category $it",
                total = Random.nextDouble(100.0, 1000.0),
                userDisplayName = "Name $it",
                note = if (it % 2 == 0) "Note $it" else "",
                date = Date(),
                userId = "userId",
            )
        }
    }
}
