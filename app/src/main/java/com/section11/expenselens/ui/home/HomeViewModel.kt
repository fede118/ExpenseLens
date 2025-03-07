package com.section11.expenselens.ui.home

import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.section11.expenselens.domain.models.HouseholdInvite
import com.section11.expenselens.domain.models.UserData
import com.section11.expenselens.domain.models.UserHousehold
import com.section11.expenselens.domain.usecase.HouseholdInvitationUseCase
import com.section11.expenselens.domain.usecase.HouseholdUseCase
import com.section11.expenselens.domain.usecase.SignInUseCase
import com.section11.expenselens.framework.credentials.GoogleCredentialManager
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToCameraScreen
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensesHistory
import com.section11.expenselens.ui.common.AbstractViewModel
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdLoading
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.AddUserToHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToExpensesHistoryTapped
import com.section11.expenselens.ui.home.mapper.HomeScreenUiMapper
import com.section11.expenselens.ui.home.mapper.PendingInvitationsMapper
import com.section11.expenselens.ui.home.model.CakeGraphUiModel
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LongParameterList")// Added ticket to think of something. Right now I like how responsibilities are divided
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val credentialManager: GoogleCredentialManager,
    private val signInUseCase: SignInUseCase,
    private val householdUseCase: HouseholdUseCase,
    private val householdInvitationUseCase: HouseholdInvitationUseCase,
    private val uiMapper: HomeScreenUiMapper,
    private val pendingInvitesMapper: PendingInvitationsMapper,
    private val dispatcher: CoroutineDispatcher
) : AbstractViewModel() {

    private val _profileDialogUiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val profileDialogUiEvent: SharedFlow<DownstreamUiEvent> = _profileDialogUiEvent

    init {
        getSignInOrSignedOutStatus()
    }

    private fun getSignInOrSignedOutStatus() {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(Loading(true))
            val userData = signInUseCase.getCurrentUser().getOrNull()
            if (userData != null) {
                onSignIn(userData)
            } else {
                _uiState.value = UserSignedOut(uiMapper.getGreeting())
            }
            _uiEvent.emit(Loading(false))
        }
    }

    private suspend fun onSignIn(userData: UserData) {
        val householdsResult = householdUseCase.getCurrentHousehold(userData.id)
        val pendingInvites = householdInvitationUseCase.getPendingInvitations(userData.id)
        _uiState.value = uiMapper.getUserSignInModel(
            userData,
            householdsResult,
            pendingInvites.getOrNull()
        )
    }

    fun onUiEvent(homeEvent: HomeUpstreamEvent) {
        viewModelScope.launch(dispatcher) {
            when(homeEvent) {
                is AddExpenseTapped -> navigationManager.navigate(NavigateToCameraScreen)
                is SignInTapped -> handleSignInEvent(homeEvent)
                is SignOutTapped -> handleSignOutEvent()
                is ToExpensesHistoryTapped -> navigationManager.navigate(NavigateToExpensesHistory)
                is CreateHouseholdTapped -> handleHouseholdCreation(homeEvent)
                is AddUserToHouseholdTapped -> handleInvitingUserToHousehold(homeEvent)
                is HouseholdInviteTap -> handleHouseholdInviteTap(homeEvent)
            }
        }
    }

    @Suppress("SwallowedException") // The user cancelled the sign in, that's why we swallow the exception
    private suspend fun handleSignInEvent(signInEvent: SignInTapped) {
        _uiEvent.emit(Loading(true))
        val credentialResponse = try {
            credentialManager.getCredentials(signInEvent.context)

        } catch (cancellationException: GetCredentialCancellationException) {
            return _uiEvent.emit(Loading(false))
        }
        when (credentialResponse.credential) {
            is CustomCredential -> {
                if (credentialResponse.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    signInUseCase.signInCredentialsFetched(credentialResponse).fold(
                        onFailure = { _uiEvent.emit(ShowSnackBar(uiMapper.getGenericErrorMessage())) },
                        onSuccess = { userData ->
                            onSignIn(userData)
                            _uiEvent.emit(Loading(false))
                        }
                    )
                }
            }
            else -> return _uiEvent.emit(Loading(false))
        }
    }

    private suspend fun handleSignOutEvent() {
        _uiEvent.emit(Loading(true))
        signInUseCase.signOut()
        _uiState.value = UserSignedOut(uiMapper.getGreeting())
        _uiEvent.emit(Loading(false))
        _uiEvent.emit(ShowSnackBar(uiMapper.getSignOutSuccessMessage()))
    }

    private suspend fun handleHouseholdCreation(event: CreateHouseholdTapped) {
        _uiEvent.emit(Loading(true))
        val houseHoldResult = householdUseCase.createHousehold(
            event.userId,
            event.householdName
        )

        houseHoldResult.fold(
            onSuccess = { household ->
                _uiState.update {
                    if (it is UserSignedIn) {
                        uiMapper.updateSignedInUiWhenHouseholdCreated(it, household)
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

    private suspend fun handleInvitingUserToHousehold(inviteEvent: AddUserToHouseholdTapped) {
        _profileDialogUiEvent.emit(AddUserToHouseholdLoading(true))
        val household = (_uiState.value as? UserSignedIn)?.householdInfo
        household?.let {
            val  result = householdInvitationUseCase.inviteToHousehold(
                inviterId = inviteEvent.invitingUserId,
                inviteeEmail = inviteEvent.inviteeUserEmail,
                household = UserHousehold(it.id, it.name)
            )

            result.fold(
                onFailure = { exception ->
                    _profileDialogUiEvent.emit(
                        pendingInvitesMapper.getHouseholdInviteResultEvent(exception)
                    )
                },
                onSuccess = {
                    _profileDialogUiEvent.emit(pendingInvitesMapper.getHouseholdInviteResultEvent())
                }
            )
        }
    }

    private suspend fun handleHouseholdInviteTap(inviteTap: HouseholdInviteTap) {
        _uiState.update { pendingInvitesMapper.setPendingInviteLoading(it, inviteTap) }

        with (inviteTap) {
            val newPendingInvitesResult = householdInvitationUseCase.handleHouseholdInviteResponse(
                accepted,
                inviteId,
                userId,
                householdId,
                householdName
            )

            var pendingInvites: List<HouseholdInvite>? = null
            newPendingInvitesResult.fold(
                onSuccess = {  pendingInvites = it },
                onFailure = {
                    pendingInvites = householdInvitationUseCase
                        .getPendingInvitations(inviteTap.userId)
                        .getOrNull()
                }
            )
            _uiState.update {
                if (it is UserSignedIn) {
                    val household = householdUseCase.getCurrentHousehold(userId)
                    pendingInvitesMapper.updateInvitesAndHousehold(it, pendingInvites, household)
                } else {
                    it
                }
            }
        }
    }

    fun updateHomeInformation() {
        getSignInOrSignedOutStatus()
    }

    // TODO remove this on first release version of the app
    fun dummyButtonForTesting(context: Context) {
        val fakeRepo = FakeRepositoryForPreviews(context)
        viewModelScope.launch(dispatcher) {
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
            data class HouseholdUiState(
                val id: String,
                val name: String,
                val graphInfo: CakeGraphUiModel?
            )
        }
        data class UserSignedOut(val greeting: String) : HomeUiState()
    }
}
