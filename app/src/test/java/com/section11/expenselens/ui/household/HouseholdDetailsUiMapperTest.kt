package com.section11.expenselens.ui.household

import com.section11.expenselens.R
import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.framework.utils.ResourceProvider
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class HouseholdDetailsUiMapperTest {

    private val resourceProvider: ResourceProvider = mock()

    private val householdDetailsUiMapper = HouseholdDetailsUiMapper(resourceProvider)

    @Before
    fun setup() {
        whenever(resourceProvider.getString(R.string.household_details_delete_household_label))
            .thenReturn("Delete Household")
        whenever(resourceProvider.getString(R.string.household_details_leave_household_label))
            .thenReturn("Leave Household")
    }

    @Test
    fun `getHouseholdDetailsUiModel returns household details`() {
        // Given
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")
        val householdDetailsWithUserEmails = HouseholdDetailsWithUserEmails(
            currentUserId = "user123",
            householdId = "household123",
            name = householdName,
            usersEmails = usersIds
        )

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdDetailsWithUserEmails)

        // Then
        assert(result.householdName == householdName)
        assert(result.users == usersIds)
    }

    @Test
    fun `getHouseholdDetailsUiModel has Leave cta if more than 1 user`() {
        // Given
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")
        val householdDetailsWithUserEmails = HouseholdDetailsWithUserEmails(
            currentUserId = "user123",
            householdId = "household123",
            name = householdName,
            usersEmails = usersIds
        )

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdDetailsWithUserEmails)

        // Then
        assert(result.cta is Leave)
    }

    @Test
    fun `getHouseholdDetailsUiModel has Delete cta if only 1 user`() {
        // Given
        val householdName = "Test Household"
        val usersIds = listOf("user123")
        val householdDetailsWithUserEmails = HouseholdDetailsWithUserEmails(
            currentUserId = "user123",
            householdId = "household123",
            name = householdName,
            usersEmails = usersIds
        )

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdDetailsWithUserEmails)

        // Then
        assert(result.cta is Delete)
    }

    @Test
    fun `getNoHouseholdIdError returns error message`() {
        // Given
        val errorMessage = "No household id found"
        whenever(resourceProvider.getString(R.string.household_details_no_household_id_error))
            .thenReturn(errorMessage)

        // When
        val result = householdDetailsUiMapper.getNoHouseholdIdError()

        // Then
        assert(result == errorMessage)
    }

    @Test
    fun `getLeaveHouseholdSuccessMessage returns success message`() {
        // Given
        val householdName = "Test Household"
        val successMessage = "Successfully left household"
        whenever(resourceProvider.getString(R.string.household_details_leave_household_success, householdName))
            .thenReturn(successMessage)

        // When
        val result = householdDetailsUiMapper.getLeaveHouseholdSuccessMessage(householdName)

        // Then
        assert(result == successMessage)
    }

    @Test
    fun `getLeaveHouseholdErrorMessage returns error message`() {
        // Given
        val errorMessage = "Failed to leave household"
        whenever(resourceProvider.getString(R.string.generic_error_message))
            .thenReturn(errorMessage)

        // When
        val result = householdDetailsUiMapper.getLeaveHouseholdErrorMessage()

        // Then
        assert(result == errorMessage)
    }

    @Test
    fun `getHouseholdDeletedSuccessMessage returns success message`() {
        // Given
        val householdName = "Test Household"
        val successMessage = "Successfully deleted household"
        whenever(resourceProvider.getString(R.string.household_details_delete_household_success, householdName))
            .thenReturn(successMessage)

        // When
        val result = householdDetailsUiMapper.getHouseholdDeletedSuccessMessage(householdName)

        // Then
        assert(result == successMessage)
    }
}
