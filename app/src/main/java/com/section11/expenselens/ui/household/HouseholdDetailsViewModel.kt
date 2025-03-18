package com.section11.expenselens.ui.household

import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.HouseholdDetailsUseCase
import com.section11.expenselens.ui.common.AbstractViewModel
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent.OnCtaClicked
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UpstreamUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseholdDetailsViewModel @Inject constructor(
    private val householdDetailsUiMapper: HouseholdDetailsUiMapper,
    private val householdDetailsUseCase: HouseholdDetailsUseCase,
    dispatcher: CoroutineDispatcher
) : AbstractViewModel() {

    init {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(Loading(true))
            householdDetailsUseCase.getCurrentHouseholdDetails().fold(
                onSuccess = { householdDetails ->
                    _uiState.value = ShowHouseholdDetails(
                        householdDetailsUiMapper.getHouseholdDetailsUiModel(
                            householdDetails.name,
                            householdDetails.usersEmails
                        )
                    )
                    _uiEvent.emit(Loading(false))
                },
                onFailure = {
                    _uiState.value = UiState.Error(householdDetailsUiMapper.getNoHouseholdIdError())
                    _uiEvent.emit(Loading(false))
                }
            )
        }
    }

    fun onHouseholdDetailsUpstreamEvent(event: HouseholdDetailsUpstreamEvent) {
        when(event) {
            is OnCtaClicked -> { /* TODO */ }
        }
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
