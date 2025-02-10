package com.section11.expenselens.ui.home

import android.content.Context
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUiEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUiEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUiEvent.SignOutTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val navigationManager: NavigationManager = mock()
    private val mapper: HomeScreenUiMapper = mock()
    private val googleSignInUseCase: GoogleSignInUseCase = mock()
    private val greeting = "hello"

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        whenever(mapper.getGreeting()).thenReturn(greeting)
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(navigationManager, googleSignInUseCase, mapper, testDispatcher)
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

        viewModel = HomeViewModel(navigationManager, googleSignInUseCase, mapper, testDispatcher)
        advanceUntilIdle()

        assertEquals(UserSignedIn(greeting, mockUiModel), viewModel.uiState.value)
    }

    @Test
    fun `on init should get user information if null update uiState to user signed out`() = runTest {
        whenever(googleSignInUseCase.getCurrentUser()).thenReturn(Result.failure(mock()))

        viewModel = HomeViewModel(navigationManager, googleSignInUseCase, mapper, testDispatcher)
        advanceUntilIdle()

        assertEquals(UserSignedOut(greeting), viewModel.uiState.value)
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
        whenever(googleSignInUseCase.signInToGoogle(mockContext))
            .thenReturn(Result.success(mockUserData))
        val mockUiModel: UserInfoUiModel = mock()
        whenever(mapper.getUserData(any())).thenReturn(mockUiModel)

        viewModel.onUiEvent(SignInTapped(mockContext))
        advanceUntilIdle()

        assertEquals(UserSignedIn(greeting, mockUiModel), viewModel.uiState.value)
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
        viewModel.onUiEvent(SignOutTapped)
        advanceUntilIdle()

        verify(googleSignInUseCase).signOut()
        assertTrue(viewModel.uiState.value is UserSignedOut)
    }
}
