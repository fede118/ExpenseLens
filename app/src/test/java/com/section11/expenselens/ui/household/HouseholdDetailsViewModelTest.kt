package com.section11.expenselens.ui.household

import com.section11.expenselens.domain.models.HouseholdDetailsWithUserEmails
import com.section11.expenselens.domain.usecase.HouseholdDetailsUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent.OnCtaClicked
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave
import com.section11.expenselens.ui.utils.UiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdDetailsViewModelTest {

    private val householdDetailsUiMapper: HouseholdDetailsUiMapper = mock()
    private val householdDetailsUseCase: HouseholdDetailsUseCase = mock()
    private val navigationManager: NavigationManager = mock()
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `on init ViewModel then should get household details`() = runTest {
        // Given
        val householdDetails = HouseholdDetailsWithUserEmails(
            "userId",
            "household123",
            "Test Household",
            listOf("email", "email2")
        )
        whenever(householdDetailsUseCase.getCurrentHouseholdDetails())
            .thenReturn(Result.success(householdDetails))
        whenever(householdDetailsUiMapper.getHouseholdDetailsUiModel(any()))
            .thenReturn(
                HouseholdDetailsUiModel(
                    userId = householdDetails.currentUserId,
                    householdId = householdDetails.householdId,
                    householdName = householdDetails.name,
                    users = householdDetails.usersEmails,
                    cta = Leave("Leave Household")
                )
            )

        // When
        val viewModel = HouseholdDetailsViewModel(
            householdDetailsUiMapper,
            householdDetailsUseCase,
            navigationManager,
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
            navigationManager,
            dispatcher
        )

        // Then
        val result = viewModel.uiState.value as? UiState.Error
        assert(result != null)
        assertEquals("Error", result?.message)
    }

    @Test
    fun `onCtaClickedEvent with Leave then should call leave and navigate back`() = runTest(dispatcher) {
        // Given
        val currentUserId = "userId"
        val householdId = "household123"
        val viewModel = initViewModel(userId = currentUserId, householdId = householdId)

        // When
        viewModel.onHouseholdDetailsUpstreamEvent(OnCtaClicked(Leave("Leave Household")))
        advanceUntilIdle()

        // Then
        verify(householdDetailsUseCase).leaveHousehold(currentUserId, householdId)
        verify(navigationManager).navigate(NavigateHome(true))
    }

    @Test
    fun `onCtaClickedEvent with Delete then should call delete and navigate back`() = runTest(dispatcher) {
        // Given
        val currentUserId = "userId"
        val householdId = "household123"
        val deleteCta = Delete("Delete Household")
        val viewModel = initViewModel(
            userId = currentUserId,
            householdId = householdId,
            cta = deleteCta
        )

        // When
        viewModel.onHouseholdDetailsUpstreamEvent(OnCtaClicked(deleteCta))
        advanceUntilIdle()

        // Then
        verify(householdDetailsUseCase).deleteHousehold(currentUserId, householdId)
        verify(navigationManager).navigate(NavigateHome(true))
    }

    private suspend fun initViewModel(
        userId: String = "userId",
        householdId: String = "household123",
        householdName: String = "Test Household",
        userEmails: List<String> = listOf("email", "email2"),
        cta: HouseholdDetailsCta = Leave("Leave Household")
    ): HouseholdDetailsViewModel {
        val householdDetails = HouseholdDetailsUiModel(
            userId = userId,
            householdId = householdId,
            householdName = householdName,
            users = userEmails,
            cta = cta
        )
        whenever(householdDetailsUseCase.getCurrentHouseholdDetails())
            .thenReturn(Result.success(mock()))
        whenever(householdDetailsUiMapper.getHouseholdDetailsUiModel(any()))
            .thenReturn(householdDetails)
        whenever(householdDetailsUseCase.leaveHousehold(any(), any()))
            .thenReturn(Result.success(Unit))
        whenever(householdDetailsUiMapper.getLeaveHouseholdSuccessMessage(any()))
            .thenReturn("Success")
        whenever(householdDetailsUiMapper.getHouseholdDeletedSuccessMessage(any()))
            .thenReturn("Success")
        whenever(navigationManager.navigate(any<NavigationEvent>())).thenReturn(Unit)

        return HouseholdDetailsViewModel(
            householdDetailsUiMapper,
            householdDetailsUseCase,
            navigationManager,
            dispatcher
        )
    }
}
