package com.section11.expenselens.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInCancelled
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInSuccess
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToCameraScreen
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignOutTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val mapper: HomeScreenUiMapper,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val uiEvent: SharedFlow<DownstreamUiEvent> = _uiEvent

    private val greeting = mapper.getGreeting()

    init {
        viewModelScope.launch(dispatcher) {
            val userData = googleSignInUseCase.getCurrentUser().getOrNull()
            if (userData != null) {
                val userUiModel = mapper.getUserData(userData)
                _uiState.value = UserSignedIn(greeting, userUiModel)
            } else {
                _uiState.value = UserSignedOut(greeting)
            }
        }
    }

    fun onUiEvent(homeEvent: HomeUpstreamEvent) {
        viewModelScope.launch(dispatcher) {
            when(homeEvent) {
                is AddExpenseTapped -> navigationManager.navigate(NavigateToCameraScreen)
                is SignInTapped -> {
                    _uiEvent.emit(Loading(true))
                    val signInResult = googleSignInUseCase.signInToGoogle(homeEvent.context).getOrNull()
                    val greeting = mapper.getGreeting()
                    when(signInResult) {
                        is SignInSuccess -> {
                            val userUiModel = mapper.getUserData(signInResult.userData)
                            _uiState.value = UserSignedIn(greeting, userUiModel)
                        }
                        is SignInCancelled -> {
                            _uiEvent.emit(Loading(false))

                        }

                        null -> _uiEvent.emit(ShowSnackBar("Something went wrong, try again"))
                    }
                    _uiEvent.emit(Loading(false))
                }
                is SignOutTapped -> {
                    _uiEvent.emit(Loading(true))
                    googleSignInUseCase.signOut()
                    _uiState.value = UserSignedOut(greeting)
                    _uiEvent.emit(Loading(false))
                    _uiEvent.emit(ShowSnackBar(mapper.getSignOutSuccessMessage()))
                }
            }
        }
    }

    fun dummyButtonForTesting(context: Context) {
        val fakeRepo = FakeRepositoryForPreviews(context)
        viewModelScope.launch {
            navigationManager.navigate(
                NavigateToExpensePreview(
                    extractedText = fakeRepo.getExtractedText(),
                    expenseInformation = fakeRepo.getExpenseInformation()
                )
            )
        }
    }

    sealed class HomeUiState : UiState() {
        data class UserSignedIn(val greeting: String, val user: UserInfoUiModel) : HomeUiState()
        data class UserSignedOut(val greeting: String) : HomeUiState()
    }
}
