package com.section11.expenselens.ui.home.mapper

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.utils.getUserData
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
        val id = "id"
        val name = "name"
        val profilePic = "img"
        val mockUserData = getUserData(
            id = id,
            name = name,
            profilePic = profilePic
        )

        val result = mapper.getUserSignInModel(mockUserData, null, null)

        with(result.user) {
            assert(result.householdInfo == null)
            assertEquals(id, id)
            assertEquals(name, displayName)
            assertEquals(profilePic, profilePic)
        }
    }

    @Test
    fun `when getUserData then userData model is returned with household info`() {
        val mockUserData = getUserData()
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
    fun `updateSignedInUiWithHousehold should return correct updated model`() {
        val mockUserData = getUserData()
        val household = UserHousehold("id", "name")

        val result = mapper.getUserSignInModel(mockUserData, household, null)
        val updatedResult = mapper.updateSignedInUiWithHousehold(result, household)

        assertEquals(household.id, updatedResult.householdInfo?.id)
        assertEquals(household.name, updatedResult.householdInfo?.name)
    }

    @Test
    fun `getGenericErrorMessage should return correct error message`() {
        whenever(resourceProvider.getString(R.string.generic_error_message))
            .thenReturn("Generic error message")

        mapper.getGenericErrorMessage()

        verify(resourceProvider).getString(R.string.generic_error_message)
    }
}
