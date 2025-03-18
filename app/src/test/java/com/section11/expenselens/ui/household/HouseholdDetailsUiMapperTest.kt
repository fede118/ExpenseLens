package com.section11.expenselens.ui.household

import com.section11.expenselens.R
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

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdName, usersIds)

        // Then
        assert(result.householdName == householdName)
        assert(result.users == usersIds)
    }

    @Test
    fun `getHouseholdDetailsUiModel has Leave cta if more than 1 user`() {
        // Given
        val householdName = "Test Household"
        val usersIds = listOf("user123", "user456")

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdName, usersIds)

        // Then
        assert(result.cta is Leave)
    }

    @Test
    fun `getHouseholdDetailsUiModel has Delete cta if only 1 user`() {
        // Given
        val householdName = "Test Household"
        val usersIds = listOf("user123")

        // When
        val result = householdDetailsUiMapper.getHouseholdDetailsUiModel(householdName, usersIds)

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
}
