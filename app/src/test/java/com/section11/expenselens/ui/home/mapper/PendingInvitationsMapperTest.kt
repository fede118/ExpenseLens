package com.section11.expenselens.ui.home.mapper

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.section11.expenselens.R
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.HouseholdInvite.HouseholdInviteStatus.Pending
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.model.CakeGraphUiModel
import com.section11.expenselens.ui.home.model.InviteStatusUiModel
import com.section11.expenselens.ui.home.model.PendingInvitesUiModel
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class PendingInvitationsMapperTest {

    private val resourceProvider: ResourceProvider = mock()

    private lateinit var mapper: PendingInvitationsMapper

    @Before
    fun setup() {
        mapper = PendingInvitationsMapper(resourceProvider)
    }

    @Test
    fun `getHouseholdInviteResultEvent should return correct error message when UserNotFoundException`() {
        val userNotFoundException = UserNotFoundException("message")
        whenever(resourceProvider.getString(R.string.home_screen_household_invite_user_not_found))
            .thenReturn("message")

        val result = mapper.getHouseholdInviteResultEvent(userNotFoundException)

        verify(resourceProvider).getString(R.string.home_screen_household_invite_user_not_found)
        assertEquals("message", result.message)
        assertEquals(Color.Red, result.textColor)
    }

    @Test
    fun `getInvitationErrorMessage should return correct error message when other exception`() {
        val exception = Exception("message")
        whenever(resourceProvider.getString(R.string.home_screen_household_invite_failure))
            .thenReturn("message")

        val result = mapper.getHouseholdInviteResultEvent(exception)

        verify(resourceProvider).getString(R.string.home_screen_household_invite_failure)
        assertEquals("message", result.message)
    }

    @Test
    fun `getSuccessInvitationMessage should return correct success message`() {
        whenever(resourceProvider.getString(R.string.home_screen_household_invite_success))
            .thenReturn("message")

        val result = mapper.getHouseholdInviteResultEvent()

        verify(resourceProvider).getString(R.string.home_screen_household_invite_success)
        assertEquals("message", result.message)
        assertEquals(Color.Green, result.textColor)
    }

    @Test
    fun `updateInvitesAndHousehold should return user with updated pending invites`() {
        val userSignedInState = UserSignedIn(
            greeting = "greeting",
            user = UserInfoUiModel(
                id = "user123",
                displayName = "John Doe",
                profilePic = "profilePic",
                pendingInvites = listOf(
                    PendingInvitesUiModel(
                        inviteId = "inviteId",
                        householdId = "household456",
                        householdName = "Test Household",
                        timestamp = Timestamp.now(),
                        status = InviteStatusUiModel.Pending,
                        isLoading = false
                    )
                )
            ),
            householdInfo = HouseholdUiState(
                "id",
                "name",
                CakeGraphUiModel(emptyList(), "centerText")
            )
        )

        val result = mapper.updateInvitesAndHousehold(userSignedInState, emptyList(), null)

        assertThat(result.user.pendingInvites).isEmpty()
    }

    @Test
    fun `updateInvitesAndHousehold should return user with updated pending invites and household`() {
        val householdId = "household456"
        val userSignedInState = UserSignedIn(
            greeting = "greeting",
            user = UserInfoUiModel(
                id = "user123",
                displayName = "John Doe",
                profilePic = "profilePic",
                pendingInvites = emptyList()
            )
        )
        val newHousehold = UserHousehold(householdId, "Test Household")
        val householdExpenses = HouseholdExpenses(newHousehold, emptyList())
        val newInvites = listOf(
            HouseholdInvite(
                inviteId = "inviteId",
                householdId = householdId,
                householdName = "Test Household",
                inviterId = "inviterId",
                timestamp = Timestamp.now(),
                status = Pending,
            )
        )

        val result = mapper.updateInvitesAndHousehold(userSignedInState, newInvites, householdExpenses)

        assertThat(result.user.pendingInvites).isNotEmpty()
        assertThat(result.householdInfo).isNotNull()
        assertThat(result.householdInfo?.id).isEqualTo(householdId)
        assertThat(result.user.pendingInvites.first().householdId).isEqualTo(householdId)
    }

    @Test
    fun `setPendingInviteLoading should return user with pending invite loading`() {
        val householdId = "household456"
        val householdName = "Test Household"
        val inviteId = "inviteId"
        val userSignedInState = UserSignedIn(
            greeting = "greeting",
            user = UserInfoUiModel(
                id = "user123",
                displayName = "John Doe",
                profilePic = "profilePic",
                pendingInvites = listOf(
                    PendingInvitesUiModel(
                        inviteId = inviteId,
                        householdId = householdId,
                        householdName = householdName,
                        timestamp = Timestamp.now(),
                        status = InviteStatusUiModel.Pending,
                        isLoading = false
                    )
                )
            )
        )

        val inviteTap = HouseholdInviteTap(
            inviteId,
            householdId,
            householdName,
            "userId",
            true
        )

        val result = mapper.setPendingInviteLoading(userSignedInState, inviteTap)

        assertThat(result as UserSignedIn).isNotNull()
        assertThat(result.user.pendingInvites.first().isLoading).isTrue()
    }

    @Test
    fun `toPendingInvitesUiModel should return correct list of pending invites`() {
        val householdId = "household456"
        val householdName = "Test Household"
        val inviteId = "inviteId"
        val timestamp = Timestamp(Date())
        val pendingInvites = listOf(
            HouseholdInvite(
                inviteId = inviteId,
                householdId = householdId,
                householdName = householdName,
                inviterId = "inviterId",
                timestamp = timestamp,
                status = Pending,
            )
        )

        val result = pendingInvites.toPendingInvitesUiModel()

        assertThat(result.first().inviteId).isEqualTo(inviteId)
        assertThat(result.first().householdId).isEqualTo(householdId)
        assertThat(result.first().householdName).isEqualTo(householdName)
        assertThat(result.first().timestamp).isEqualTo(timestamp)
        assertThat(result.first().status).isEqualTo(InviteStatusUiModel.Pending)
        assertThat(result.first().isLoading).isFalse()
    }
}
