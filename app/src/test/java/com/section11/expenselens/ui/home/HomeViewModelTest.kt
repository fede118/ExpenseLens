package com.section11.expenselens.ui.home

import android.content.Context
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInCancelled
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInSuccess
import com.section11.expenselens.domain.usecase.StoreExpenseUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignOutTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val navigationManager: NavigationManager = mock()
    private val mapper: HomeScreenUiMapper = mock()
    private val googleSignInUseCase: GoogleSignInUseCase = mock()
    private val storeExpenseUseCase: StoreExpenseUseCase = mock()
    private val greeting = "hello"

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        whenever(mapper.getGreeting()).thenReturn(greeting)
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(
            navigationManager,
            googleSignInUseCase,
            storeExpenseUseCase,
            mapper,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `on init should get user information to update uiState to user signed in`() = runTest {
        val mockUserData = UserData("id", "idToken", "name", "img")
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        val mockUiModel: UserInfoUiModel = mock()
        whenever(mapper.getUserData(any())).thenReturn(mockUiModel)
        val user: UserData = mock()
        whenever(user.id).thenReturn("uid")
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.success(user))

        viewModel = HomeViewModel(
            navigationManager,
            googleSignInUseCase,
            storeExpenseUseCase,
            mapper,
            testDispatcher
        )
        advanceUntilIdle()

        assertEquals(UserSignedIn(greeting, mockUiModel), viewModel.uiState.value)
    }

    @Test
    fun `on init should get user information if null update uiState to user signed out`() = runTest {
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.failure(mock()))

        viewModel = HomeViewModel(
            navigationManager,
            googleSignInUseCase,
            storeExpenseUseCase,
            mapper,
            testDispatcher
        )
        advanceUntilIdle()

        assertEquals(UserSignedOut(greeting), viewModel.uiState.value)
    }

    @Test
    fun `on init if user signed in then should try to get household`() = runTest {
        val mockUserData = UserData("id", "idToken", "name", "img")
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.success(mockUserData))
        val mockUiModel: UserInfoUiModel = mock()
        whenever(mapper.getUserData(any())).thenReturn(mockUiModel)
        val user: UserData = mock()
        whenever(user.id).thenReturn("uid")
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.success(user))

        viewModel = HomeViewModel(
            navigationManager,
            googleSignInUseCase,
            storeExpenseUseCase,
            mapper,
            testDispatcher
        )
        advanceUntilIdle()

        verify(storeExpenseUseCase).getCurrentHouseholdIdAndName(any())
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
        val mockUserData = UserData("id", "idToken", "name", "img")
        val successSigning = SignInSuccess(mockUserData)
        whenever(googleSignInUseCase.signInToGoogle(mockContext))
            .thenReturn(Result.success(successSigning))
        val mockUiModel: UserInfoUiModel = mock()
        whenever(mapper.getUserData(any())).thenReturn(mockUiModel)

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assertEquals(UserSignedIn(greeting, mockUiModel), viewModel.uiState.value)
    }

    @Test
    fun `when SignInTapped event and user cancels then should hide loader`() = runTest {
        val mockContext: Context = mock()
        whenever(googleSignInUseCase.signInToGoogle(mockContext))
            .thenReturn(Result.success(SignInCancelled))

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
        verify(mapper, never()).getUserData(any())
    }

    @Test
    fun `when SignInTapped event and sign in fails then should update ui state`() = runTest {
        val mockContext: Context = mock()
        whenever(googleSignInUseCase.signInToGoogle(mockContext))
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

        verify(googleSignInUseCase).signOut()
        assertTrue(viewModel.uiState.value is UserSignedOut)
    }
}
