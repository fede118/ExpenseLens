package com.section11.expenselens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToCameraScreen
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUiEvent
import com.section11.expenselens.ui.home.event.HomeUiEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUiEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUiEvent.SignOutTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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

    fun onUiEvent(homeEvent: HomeUiEvent) {
        viewModelScope.launch(dispatcher) {
            when(homeEvent) {
                is AddExpenseTapped -> navigationManager.navigate(NavigateToCameraScreen)
                is SignInTapped -> {
                    val userData = googleSignInUseCase.signInToGoogle(homeEvent.context).getOrNull()
                    val greeting = mapper.getGreeting()
                    if (userData != null) {
                        val userUiModel = mapper.getUserData(userData)
                        _uiState.value = UserSignedIn(greeting, userUiModel)
                    } else {
                        // This should be an actual error state and a snackbar, this is just for testing
                        _uiState.value = UserSignedOut("Something went wrong")
                    }
                }
                is SignOutTapped -> {
                    googleSignInUseCase.signOut()
                    _uiState.value = UserSignedOut(greeting)
                }
            }
        }
    }

    sealed class HomeUiState : UiState() {
        data class UserSignedIn(val greeting: String, val user: UserInfoUiModel) : HomeUiState()
        data class UserSignedOut(val greeting: String) : HomeUiState()
    }
}
