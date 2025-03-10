package com.section11.expenselens.ui.home

import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.section11.expenselens.domain.models.HouseholdExpenses
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.usecase.HouseholdInvitationUseCase
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.framework.credentials.GoogleCredentialManager
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdLoading
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.HouseholdInviteResultEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.AddUserToHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.mapper.PendingInvitationsMapper
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.getUserData
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val navigationManager: NavigationManager = mock()
    private val credentialManager: GoogleCredentialManager = mock()
    private val mapper: HomeScreenUiMapper = mock()
    private val invitesMapper: PendingInvitationsMapper = mock()
    private val signInUseCase: SignInUseCase = mock()
    private val householdUseCase: HouseholdUseCase = mock()
    private val householdInvitationsUseCase: HouseholdInvitationUseCase = mock()
    private val greeting = "hello"

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        whenever(mapper.getGreeting()).thenReturn(greeting)
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `on init should get user information to update uiState to user signed in`() = runTest {
        mockInitWithSomeUserInfo()

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
        advanceUntilIdle()

        assert(viewModel.uiState.value is UserSignedIn)
    }

    @Test
    fun `on init should get user information if null update uiState to user signed out`() = runTest {
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.failure(mock()))

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
        advanceUntilIdle()

        assertEquals(UserSignedOut(greeting), viewModel.uiState.value)
    }

    @Test
    fun `on init if user signed in then should try to get household`() = runTest {
        mockInitWithSomeUserInfo()

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
        advanceUntilIdle()

        verify(householdUseCase).getCurrentHousehold(any())
    }

    @Test
    fun `when add expense tap event then should navigate to camera`() = runTest {
        val expenseTapEvent = AddExpenseTapped

        viewModel.onUiEvent(expenseTapEvent)
        advanceUntilIdle()

        verify(navigationManager).navigate(NavigationEvent.NavigateToCameraScreen)
    }

    @Test
    fun `when SignInTapped event and sign in successful then should update ui state`() = runTest {
        val mockContext: Context = mock()
        val mockUserData = getUserData()
        val mockCredentialResponse: GetCredentialResponse = mock()
        val mockCredential: CustomCredential = mock()
        whenever(mockCredentialResponse.credential).thenReturn(mockCredential)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        whenever(credentialManager.getCredentials(mockContext)).thenReturn(mockCredentialResponse)
        whenever(signInUseCase.signInCredentialsFetched(any()))
            .thenReturn(Result.success(mockUserData))
        val mockUiModel: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assert(viewModel.uiState.value is UserSignedIn)
    }

    @Test
    fun `when SignInTapped event and sign in successful then should update currentHousehold with Id`() = runTest {
        val mockContext: Context = mock()
        val mockUserData = getUserData(currentHouseHoldId = null)
        val mockCredentialResponse: GetCredentialResponse = mock()
        val mockCredential: CustomCredential = mock()
        whenever(mockCredentialResponse.credential).thenReturn(mockCredential)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        whenever(credentialManager.getCredentials(mockContext)).thenReturn(mockCredentialResponse)
        whenever(signInUseCase.signInCredentialsFetched(any()))
            .thenReturn(Result.success(mockUserData))
        val mockUiModel: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)
        val householdExpenses: HouseholdExpenses = mock()
        val userHousehold = UserHousehold("id", "name")
        whenever(householdExpenses.householdInfo).thenReturn(userHousehold)
        whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(householdExpenses)

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assert(viewModel.uiState.value is UserSignedIn)
        verify(signInUseCase).updateCurrentHouseholdId(userHousehold.id)
    }

    @Test
    fun `when SignInTapped event and sign in successful but userData has householdId shouln't be updated`() = runTest {
        val mockContext: Context = mock()
        val userHousehold = UserHousehold("id", "name")
        val mockUserData = getUserData(currentHouseHoldId = userHousehold.id)
        val mockCredentialResponse: GetCredentialResponse = mock()
        val mockCredential: CustomCredential = mock()
        whenever(mockCredentialResponse.credential).thenReturn(mockCredential)
        whenever(mockCredential.type).thenReturn(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        whenever(credentialManager.getCredentials(mockContext)).thenReturn(mockCredentialResponse)
        whenever(signInUseCase.signInCredentialsFetched(any()))
            .thenReturn(Result.success(mockUserData))
        val mockUiModel: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)
        val householdExpenses: HouseholdExpenses = mock()
        whenever(householdExpenses.householdInfo).thenReturn(userHousehold)
        whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(householdExpenses)

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assert(viewModel.uiState.value is UserSignedIn)
        verify(signInUseCase, never()).updateCurrentHouseholdId(userHousehold.id)
    }

    @Test
    fun `when SignInTapped event and user cancels then should hide loader`() = runTest {
        val mockContext: Context = mock()
        whenever(credentialManager.getCredentials(mockContext))
            .then { throw GetCredentialCancellationException() }

        // Since this is a cold flow we need to start the collection before actually calling the viewModel method
        val job = launch {
            viewModel.uiEvent.collectIndexed { index, value ->
                if (index == 0) assert((value as? Loading)?.isLoading == true)
                if (index == 1) assert((value as? Loading)?.isLoading == false)
                cancel() // Cancel the coroutine after receiving the expected event
            }
        }

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        job.join() // Ensure the coroutine completes

        assertTrue(viewModel.uiState.value is UserSignedOut)
        verify(mapper, never()).getUserSignInModel(any(), any(), anyOrNull())
    }

    @Test
    fun `when SignInTapped event and sign in fails then should update ui state`() = runTest {
        val mockContext: Context = mock()
        val mockCredentialResponse: GetCredentialResponse = mock()
        whenever(credentialManager.getCredentials(mockContext)).thenReturn(mockCredentialResponse)
        whenever(signInUseCase.signInCredentialsFetched(mockCredentialResponse))
            .thenReturn(Result.failure(mock()))

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UserSignedOut)
    }

    @Test
    fun `when SignOutTapped event then should update ui state`() = runTest {
        whenever(mapper.getSignOutSuccessMessage()).thenReturn("sign out success")

        // Since this is a cold flow we need to start the collection before actually calling the viewModel method
        val job = launch {
            viewModel.uiEvent.collectIndexed { index, value ->
                if (index == 0) assert((value as? Loading)?.isLoading == true)
                if (index == 1) assert((value as? Loading)?.isLoading == false)
                if (index == 3) assert((value as? ShowSnackBar)?.message == "sign out success")
                cancel() // Cancel the coroutine after receiving the expected event
            }
        }

        viewModel.onUiEvent(SignOutTapped)
        advanceUntilIdle()

        job.join() // Ensure the coroutine completes

        verify(signInUseCase).signOut()
        assertTrue(viewModel.uiState.value is UserSignedOut)
    }

    @Test
    fun `when ToExpenseHistory tap then should navigate to expense history`() = runTest {
        val event = ProfileDialogEvents.ToExpensesHistoryTapped

        viewModel.onUiEvent(event)
        advanceUntilIdle()

        verify(navigationManager).navigate(NavigationEvent.NavigateToExpensesHistory)
    }

    @Test
    fun `when CreateHouseholdTapped then should create household`() = runTest {
        val event = CreateHouseholdTapped("id", "name")

        viewModel.onUiEvent(event)
        advanceUntilIdle()

        verify(householdUseCase).createHousehold(any(), any())
    }

    @Test
    fun `when household creating succeeds then should update ui state`() = runTest {
        val event = CreateHouseholdTapped("id", "name")
        val householdResult: UserHousehold = mock<UserHousehold>().apply {
            whenever(id).thenReturn("someHouseholdId")
            whenever(name).thenReturn(event.householdName)
        }
        whenever(householdUseCase.createHousehold(any(), any()))
            .thenReturn(Result.success(householdResult))
        val mockUiModel: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mock()))
        mockkStatic(UserSignedIn::class)
        whenever(mockUiModel.copy(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
        viewModel.onUiEvent(event)
        advanceUntilIdle()

        verify(mapper).updateSignedInUiWhenHouseholdCreated(mockUiModel, householdResult)
    }

    @Test
    fun `when invite user to household then should invite user`() = runTest {
        val event = AddUserToHouseholdTapped("userId", "inviteeUserEmail")
        whenever(householdInvitationsUseCase.inviteToHousehold(any(), any(), any()))
            .thenReturn(Result.success(Unit))
        mockInitWithSomeUserInfo(withHousehold = true)
        whenever(invitesMapper.getHouseholdInviteResultEvent()).thenReturn(mock())

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )

        val job = launch {
            viewModel.profileDialogUiEvent.collectIndexed { index, value ->
                if (index == 0) assert(value is AddUserToHouseholdLoading)
                if (index == 1) assert(value is HouseholdInviteResultEvent)
                cancel()
            }
        }

        viewModel.onUiEvent(event)
        advanceUntilIdle()

        job.join()

        verify(householdInvitationsUseCase).inviteToHousehold(any(), any(), any())
    }

    @Test
    fun `when invite user to household then should show error`() = runTest {
        val event = AddUserToHouseholdTapped("invitingUserId", "inviteeUserEmail")
        val mockException: Throwable = mock()
        whenever(householdInvitationsUseCase.inviteToHousehold(any(), any(), any()))
            .thenReturn(Result.failure(mockException))
        mockInitWithSomeUserInfo(withHousehold = true)
        whenever(invitesMapper.getHouseholdInviteResultEvent(mockException))
            .thenReturn(mock())

        viewModel = HomeViewModel(
            navigationManager,
            credentialManager,
            signInUseCase,
            householdUseCase,
            householdInvitationsUseCase,
            mapper,
            invitesMapper,
            testDispatcher
        )
        advanceUntilIdle()

        val job = launch {
            viewModel.profileDialogUiEvent.collectIndexed { index, value ->
                if (index == 0) assert(value is AddUserToHouseholdLoading)
                if (index == 1) assert(value is HouseholdInviteResultEvent)
                cancel()
            }
        }

        viewModel.onUiEvent(event)
        advanceUntilIdle()
        job.join()

        verify(householdInvitationsUseCase).inviteToHousehold(any(), any(), any())
    }

    @Test
    fun `onHouseholdInvite tap Accept then should update ui state`() = runTest {
        val event = HouseholdInviteTap(
            "inviteId",
            "householdId",
            "householdName",
            "userId",
            true
        )
        whenever(
            householdInvitationsUseCase.handleHouseholdInviteResponse(eq(true), any(), any(), any(), any())
        ).thenReturn(Result.success(emptyList()))
        val household = UserHousehold("householdId", "householdName")
        val householdExpenses = HouseholdExpenses(household, emptyList())
        val signedInState = mockSignIn()
        whenever(invitesMapper.setPendingInviteLoading(any(), any())).thenReturn(signedInState)
        whenever(invitesMapper.updateInvitesAndHousehold(any(), any(), any())).thenReturn(mock())
        viewModel.onUiEvent(SignInTapped(mock()))
        whenever(householdUseCase.getCurrentHousehold(anyString())).thenReturn(householdExpenses)

        viewModel.onUiEvent(event)
        advanceUntilIdle()

        verify(invitesMapper).setPendingInviteLoading(any(), any())
        verify(householdUseCase, atLeast(1)).getCurrentHousehold(any())
        verify(invitesMapper).updateInvitesAndHousehold(any(), anyOrNull(), anyOrNull())
        assertTrue(viewModel.uiState.value is UserSignedIn)
    }

    @Test
    fun `on HouseholdInviteTap refused then shouldn't add household and remove invite`() = runTest {
        val event = HouseholdInviteTap("inviteId", "id", "name", "user", false)
        whenever(
            householdInvitationsUseCase.handleHouseholdInviteResponse(eq(false), any(), any(), any(), any())
        ).thenReturn(Result.success(emptyList()))
        val signedInState = mockSignIn()
        whenever(invitesMapper.setPendingInviteLoading(any(), any())).thenReturn(signedInState)
        whenever(invitesMapper.updateInvitesAndHousehold(any(), any(), any())).thenReturn(mock())
        viewModel.onUiEvent(SignInTapped(mock()))
        whenever(householdUseCase.getCurrentHousehold(anyString())).thenReturn(mock())

        viewModel.onUiEvent(event)
        advanceUntilIdle()

        verify(householdInvitationsUseCase)
            .handleHouseholdInviteResponse(false, "inviteId", "user", "id", "name")
        assertTrue(viewModel.uiState.value is UserSignedIn)
    }

    @Test
    fun `signInToGoogle with invalid credential type should not update ui to SignedIn`() = runTest {
        val customCredential = CustomCredential("invalid_type", mock())
        val credentialResponse = GetCredentialResponse(customCredential)
        whenever(credentialManager.getCredentials(any())).thenReturn(credentialResponse)

        viewModel.onUiEvent(SignInTapped(mock()))
        advanceUntilIdle()

        assert(viewModel.uiState.value is UserSignedOut)
    }

    private suspend fun mockInitWithSomeUserInfo(withHousehold: Boolean = false) {
        val householdId = "someHouseholdId"
        val householdName = "someHouseholdName"
        val mockUserData = getUserData(currentHouseHoldId = householdId)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        val mockUiModel: UserSignedIn = mock()
        if (withHousehold) {
            val mockHouseholdInfo: UserHousehold = mock()
            whenever(mockHouseholdInfo.id).thenReturn(householdId)
            whenever(mockHouseholdInfo.name).thenReturn(householdId)
            val mockHousehold: HouseholdExpenses = mock()
            whenever(mockHousehold.householdInfo).thenReturn(mockHouseholdInfo)
            whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(mockHousehold)
            val mockHouseholdUiState: HouseholdUiState = mock()
            with(mockHouseholdUiState) {
                whenever(id).thenReturn(householdId)
                whenever(name).thenReturn(householdName)
                whenever(mockUiModel.householdInfo).thenReturn(this)
            }
        }
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)
        val user: UserData = mock()
        whenever(user.id).thenReturn("uid")
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(user))
        whenever(signInUseCase.updateCurrentHouseholdId(any())).thenReturn(mock())
    }

    @Test
    fun `updateHomeInformation should update home information`() = runTest {
        // Given
        val mockUserData = getUserData(currentHouseHoldId = null)
        val mockUiModel: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull())).thenReturn(mockUiModel)
        whenever(signInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        val mockHousehold: HouseholdExpenses = mock()
        val userHousehold = UserHousehold("id", "name")
        whenever(mockHousehold.householdInfo).thenReturn(userHousehold)
        whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(mockHousehold)

        // When
        viewModel.updateHomeInformation()
        advanceUntilIdle()

        // Then
        // called on init on the viewModel and in the updateHomeInformation
        verify(signInUseCase, times(2)).getCurrentUser()
        verify(householdUseCase).getCurrentHousehold(mockUserData.id)
        verify(householdInvitationsUseCase).getPendingInvitations(mockUserData.id)
        verify(mapper).getUserSignInModel(any(), anyOrNull(), anyOrNull())
    }

    private suspend fun mockSignIn(
        withHousehold: Boolean = false,
        withPendingInvitations: Boolean = false
    ): UserSignedIn {
        val mockUserData: UserData = mock()
        whenever(mockUserData.id).thenReturn("id")
        val mockCredentialResponse: GetCredentialResponse = mock()
        whenever(credentialManager.getCredentials(any())).thenReturn(mockCredentialResponse)
        whenever(signInUseCase.signInCredentialsFetched(any()))
            .thenReturn(Result.success(mockUserData))
        if (withHousehold) {
            val household: HouseholdExpenses = mock()
            whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(household)
        } else {
            whenever(householdUseCase.getCurrentHousehold(any())).thenReturn(null)
        }

        if (withPendingInvitations) {
            val pendingInvites: List<HouseholdInvite> = mock()
            whenever(householdInvitationsUseCase.getPendingInvitations(any()))
                .thenReturn(Result.success(pendingInvites))
        } else {
            whenever(householdInvitationsUseCase.getPendingInvitations(any()))
                .thenReturn(Result.success(emptyList()))
        }

        val mockSignedInUser: UserSignedIn = mock()
        whenever(mapper.getUserSignInModel(any(), anyOrNull(), anyOrNull()))
            .thenReturn(mockSignedInUser)
        return mockSignedInUser
    }
}
