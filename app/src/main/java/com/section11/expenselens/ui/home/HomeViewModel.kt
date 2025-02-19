package com.section11.expenselens.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInCancelled
import com.section11.expenselens.domain.usecase.GoogleSignInUseCase.SignInResult.SignInSuccess
import com.section11.expenselens.domain.usecase.StoreExpenseUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToCameraScreen
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensesHistory
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignOutTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.ToExpensesHistoryTapped
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val storeExpenseUseCase: StoreExpenseUseCase,
    private val uiMapper: HomeScreenUiMapper,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val uiEvent: SharedFlow<DownstreamUiEvent> = _uiEvent

    init {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(Loading(true))
            val userData = googleSignInUseCase.getCurrentUser().getOrNull()
            if (userData != null) {
                val householdsResult = storeExpenseUseCase.getCurrentHousehold(userData.id)
                _uiState.value = uiMapper.getUserSignInModel(userData, householdsResult)
            } else {
                _uiState.value = UserSignedOut(uiMapper.getGreeting())
            }
            _uiEvent.emit(Loading(false))
        }
    }

    fun onUiEvent(homeEvent: HomeUpstreamEvent) {
        viewModelScope.launch(dispatcher) {
            when(homeEvent) {
                is AddExpenseTapped -> navigationManager.navigate(NavigateToCameraScreen)
                is SignInTapped -> handleSignInEvent(homeEvent)
                is SignOutTapped -> handleSignOutEvent()
                is ToExpensesHistoryTapped -> navigationManager.navigate(NavigateToExpensesHistory)
                is CreateHouseholdTapped -> handleHouseholdCreation(homeEvent)
            }
        }
    }

    private suspend fun handleSignInEvent(event: SignInTapped) {
        _uiEvent.emit(Loading(true))
        when(val signInResult = googleSignInUseCase.signInToGoogle(event.context).getOrNull()) {
            is SignInSuccess -> {
                val userData = signInResult.userData
                val householdsResult = storeExpenseUseCase.getCurrentHousehold(userData.id)
                _uiState.value = uiMapper.getUserSignInModel(userData, householdsResult)
            }
            is SignInCancelled -> _uiEvent.emit(Loading(false))
            null -> _uiEvent.emit(ShowSnackBar("Something went wrong, try again"))
        }
        _uiEvent.emit(Loading(false))
    }

    private suspend fun handleSignOutEvent() {
        _uiEvent.emit(Loading(true))
        googleSignInUseCase.signOut()
        _uiState.value = UserSignedOut(uiMapper.getGreeting())
        _uiEvent.emit(Loading(false))
        _uiEvent.emit(ShowSnackBar(uiMapper.getSignOutSuccessMessage()))
    }

    private suspend fun handleHouseholdCreation(event: CreateHouseholdTapped) {
        _uiEvent.emit(Loading(true))
        val houseHoldResult = storeExpenseUseCase.createHousehold(
            event.userId,
            event.householdName
        )

        houseHoldResult.fold(
            onSuccess = { household ->
                _uiState.update {
                    if (it is UserSignedIn) {
                        it.copy(householdInfo = HouseholdUiState(
                            household.id,
                            household.name
                        ))
                    } else {
                        it
                    }
                }
                _uiEvent.emit(Loading(false))
            },
            onFailure = {
                _uiEvent.tryEmit(ShowSnackBar(uiMapper.getHouseholdCreationErrorMessage()))
                _uiEvent.emit(Loading(false))
            }
        )
    }

    // TODO remove this on first release version of the app
    fun dummyButtonForTesting(context: Context) {
        val fakeRepo = FakeRepositoryForPreviews(context)
        viewModelScope.launch {
            navigationManager.navigate(
                NavigateToExpensePreview(
                    extractedText = fakeRepo.getExtractedText(),
                    suggestedExpenseInformation = fakeRepo.getExpenseInformation()
                )
            )
        }
    }

    sealed class HomeUiState : UiState() {
        data class UserSignedIn(
            val greeting: String,
            val user: UserInfoUiModel,
            val householdInfo: HouseholdUiState? = null
        ) : HomeUiState() {
            data class HouseholdUiState(val id: String, val name: String)
        }
        data class UserSignedOut(val greeting: String) : HomeUiState()
    }
}
