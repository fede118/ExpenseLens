package com.section11.expenselens.ui.household

import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.usecase.HouseholdDetailsUseCase
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave
import com.section11.expenselens.ui.utils.UiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdDetailsViewModelTest {

    private val householdDetailsUiMapper: HouseholdDetailsUiMapper = mock()
    private val householdDetailsUseCase: HouseholdDetailsUseCase = mock()
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `on init ViewModel then should get household details`() = runTest {
        // Given
        val householdDetails = HouseholdDetailsWithUserEmails(
            "household123",
            "Test Household",
            listOf("email", "email2")
        )
        whenever(householdDetailsUseCase.getCurrentHouseholdDetails())
            .thenReturn(Result.success(householdDetails))
        whenever(householdDetailsUiMapper.getHouseholdDetailsUiModel(any() ,any()))
            .thenReturn(
                HouseholdDetailsUiModel(
                    householdName = householdDetails.name,
                    users = householdDetails.usersEmails,
                    cta = Leave("Leave Household")
                )
            )

        // When
        val viewModel = HouseholdDetailsViewModel(
            householdDetailsUiMapper,
            householdDetailsUseCase,
            dispatcher
        )

        val result = viewModel.uiState.value as? ShowHouseholdDetails
        assert(result != null)
        assertEquals(householdDetails.name , result?.householdDetails?.householdName)
        householdDetails.usersEmails.forEach {
            assert(result?.householdDetails?.users?.contains(it) == true)
        }
    }

    @Test
    fun `on viewModel init if getCurrentHouseholdDetails fails then should show error`() = runTest {
        // Given
        whenever(householdDetailsUseCase.getCurrentHouseholdDetails())
            .thenReturn(Result.failure(Exception("Error")))
        whenever(householdDetailsUiMapper.getNoHouseholdIdError()).thenReturn("Error")

        // When
        val viewModel = HouseholdDetailsViewModel(
            householdDetailsUiMapper,
            householdDetailsUseCase,
            dispatcher
        )

        // Then
        val result = viewModel.uiState.value as? UiState.Error
        assert(result != null)
        assertEquals("Error", result?.message)
    }
}
