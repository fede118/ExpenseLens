package com.section11.expenselens.ui.household

import androidx.lifecycle.viewModelScope
import com.section11.expenselens.ui.common.AbstractViewModel
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent.OnCtaClicked
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UpstreamUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseholdDetailsViewModel @Inject constructor(
    private val householdDetailsUiMapper: HouseholdDetailsUiMapper,
    dispatcher: CoroutineDispatcher
) : AbstractViewModel() {

    init {
        viewModelScope.launch(dispatcher) {
            // todo get from useCase - repository
            _uiState.value = ShowHouseholdDetails(
                // TODO get usernames from useCa
                householdDetailsUiMapper.getHouseholdDetailsUiModel(
                    "Test Household",
                    listOf("User 1", "User 2", "User 3")
                )
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
