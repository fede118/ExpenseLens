package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.R
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

        val result = mapper.getUserSignInModel(mockUserData, null)

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

        val result = mapper.getUserSignInModel(mockUserData, household)

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

        verify(resourceProvider).getString(R.string.home_screen_house_hold_creation_failure)
    }
}
