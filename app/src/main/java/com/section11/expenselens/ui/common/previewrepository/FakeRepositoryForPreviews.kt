package com.section11.expenselens.ui.common.previewrepository

import android.content.Context
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.google.firebase.Timestamp
import com.section11.expenselens.domain.getCurrentMonthName
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.Expense
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProviderImpl
import com.section11.expenselens.ui.history.mapper.ExpenseHistoryUiMapper
import com.section11.expenselens.ui.history.model.ExpenseHistoryUiItem
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.household.HouseholdDetailsUiMapper
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import java.util.Date
import kotlin.random.Random

private const val LOREM_IPSUM_SIZE = 200
private val loremIpsum = LoremIpsum(LOREM_IPSUM_SIZE).values.first()

@Suppress("MagicNumber") // this is just for Previews
class FakeRepositoryForPreviews(context: Context) {

    private val resourceProvider = ResourceProviderImpl(context)
    private val homeMapper = HomeScreenUiMapper(resourceProvider)
    private val expenseHistoryMapper = ExpenseHistoryUiMapper()
    private val householdDetailsUiMapper = HouseholdDetailsUiMapper(resourceProvider)

    fun getUserSignedInState(withHousehold: Boolean = true): UserSignedIn {
        return homeMapper.getUserSignInModel(
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
                    monthOfExpenses = getCurrentMonthName(),
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

    private fun getExpenseHistoryList(): List<Expense> {
        return List(4) {
            Expense(
                expenseId = "item$it",
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

    fun getExpenseHistoryList(size: Int = 4): List<ExpenseHistoryUiItem> {
        val expenseList =  List(size) {
            Expense(
                expenseId = "item$it",
                category = "Category $it",
                total = Random.nextDouble(100.0, 1000.0),
                userDisplayName = "Name $it",
                note = if (it % 2 == 0) "Note $it" else "",
                date = Date(),
                userId = "userId",
            )
        }

        return expenseHistoryMapper.mapExpensesToUiItems(expenseList)
    }

    fun getHouseholdDetails(usersInHousehold: Int = 1): HouseholdDetailsUiModel {
        return householdDetailsUiMapper.getHouseholdDetailsUiModel(
            HouseholdDetailsWithUserEmails(
                "id",
                "userId",
                "Preview Test Household",
                List(usersInHousehold) { "User $it" }
            )
        )
    }
}
