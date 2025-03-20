package com.section11.expenselens.ui.household

import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.HouseholdDetailsUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateHome
import com.section11.expenselens.ui.common.AbstractViewModel
import com.section11.expenselens.ui.common.UiConstants.SNACKBAR_DELAY
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent.OnCtaClicked
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Leave
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UiState.Error
import com.section11.expenselens.ui.utils.UpstreamUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseholdDetailsViewModel @Inject constructor(
    private val householdDetailsUiMapper: HouseholdDetailsUiMapper,
    private val householdDetailsUseCase: HouseholdDetailsUseCase,
    private val navigationManager: NavigationManager,
    private val dispatcher: CoroutineDispatcher
) : AbstractViewModel() {

    init {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(Loading(true))
            householdDetailsUseCase.getCurrentHouseholdDetails().fold(
                onSuccess = { householdDetails ->
                    _uiState.value = ShowHouseholdDetails(
                        householdDetailsUiMapper.getHouseholdDetailsUiModel(householdDetails)
                    )
                    _uiEvent.emit(Loading(false))
                },
                onFailure = {
                    _uiState.value = Error(householdDetailsUiMapper.getNoHouseholdIdError())
                    _uiEvent.emit(Loading(false))
                }
            )
        }
    }

    fun onHouseholdDetailsUpstreamEvent(event: HouseholdDetailsUpstreamEvent) {
        when(event) {
            is OnCtaClicked -> handleCtaClickedEvent(event.cta)
        }
    }

    private fun handleCtaClickedEvent(ctaClicked: HouseholdDetailsCta) {
        val householdDetails = (uiState.value as? ShowHouseholdDetails)?.householdDetails
        if (householdDetails == null) {
            _uiState.value = Error(householdDetailsUiMapper.getNoHouseholdIdError())
            return
        }
        with(householdDetails) {
            viewModelScope.launch(dispatcher) {
                _uiEvent.emit(Loading(true))
                when (ctaClicked) {
                    is Leave -> leaveHousehold(userId, householdId, householdName)
                    is Delete -> deleteHousehold(userId, householdId, householdName)
                }
            }
        }
    }

    private suspend fun leaveHousehold(userId: String, householdId: String, householdName: String) {
        householdDetailsUseCase.leaveHousehold(userId, householdId).fold(
            onSuccess = {
                _uiEvent.emit(Loading(false))
                _uiEvent.emit(ShowSnackBar(
                    householdDetailsUiMapper.getLeaveHouseholdSuccessMessage(householdName))
                )
                delay(SNACKBAR_DELAY)
                navigationManager.navigate(NavigateHome(shouldUpdateHome = true))
            },
            onFailure = {
                _uiEvent.emit(Loading(false))
                _uiState.value = Error(householdDetailsUiMapper.getLeaveHouseholdErrorMessage())
            }
        )
    }

    private suspend fun deleteHousehold(userId: String, householdId: String, householdName: String) {
        householdDetailsUseCase.deleteHousehold(userId, householdId).fold(
            onSuccess = {
                _uiEvent.emit(Loading(false))
                _uiEvent.emit(ShowSnackBar(
                    householdDetailsUiMapper.getHouseholdDeletedSuccessMessage(householdName))
                )
                navigationManager.navigate(NavigateHome(shouldUpdateHome = true))
            },
            onFailure = {
                _uiEvent.emit(Loading(false))
                _uiState.value = Error(householdDetailsUiMapper.getLeaveHouseholdErrorMessage())
            }
        )
    }

    sealed class HouseholdDetailsUiState : UiState() {
        data class ShowHouseholdDetails(
            val householdDetails: HouseholdDetailsUiModel
        ) : HouseholdDetailsUiState()
    }

    sealed class HouseholdDetailsUpstreamEvent : UpstreamUiEvent() {
        class OnCtaClicked(val cta: HouseholdDetailsCta) : HouseholdDetailsUpstreamEvent()
    }
}
