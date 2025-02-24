package com.section11.expenselens.ui.home.mapper

import androidx.compose.ui.graphics.Color
import com.section11.expenselens.R
import com.section11.expenselens.domain.exceptions.UserNotFoundException
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HomeScreenUiMapperTest {

    private val resourceProvider: ResourceProvider = mock()

    private lateinit var mapper: HomeScreenUiMapper

    @Before
    fun setup() {
        whenever(resourceProvider.getString(R.string.welcome_greeting)).thenReturn("Hello")
        mapper = HomeScreenUiMapper(resourceProvider)
    }

    @Test
    fun `when getting greeting greeting is returned`() {
        val greeting = mapper.getGreeting()

        assertEquals("Hello", greeting)
    }

    @Test
    fun `when getUserData then userData model is returned when household null`() {
        val mockUserData = UserData("idToken", "id", "name", "img")

        val result = mapper.getUserSignInModel(mockUserData, null, null)

        with(result.user) {
            assert(result.householdInfo == null)
            assertEquals("id", id)
            assertEquals("name", displayName)
            assertEquals("img", profilePic)
        }
    }

    @Test
    fun `when getUserData then userData model is returned with household info`() {
        val mockUserData = UserData("idToken", "id", "name", "img")
        val household = UserHousehold("id", "name")

        val result = mapper.getUserSignInModel(mockUserData, household, null)

        with(result) {
            assertEquals(household.id, householdInfo?.id)
            assertEquals(household.name, householdInfo?.name)
        }
    }

    @Test
    fun `getSignOut SuccessMessage should return sign out message`() {
        mapper.getSignOutSuccessMessage()

        verify(resourceProvider).getString(R.string.home_screen_sign_out_success)
    }

    @Test
    fun `getHouseholdCreationErrorMessage should return correct error message`() {
        mapper.getHouseholdCreationErrorMessage()

        verify(resourceProvider).getString(R.string.home_screen_household_creation_failure)
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
}
