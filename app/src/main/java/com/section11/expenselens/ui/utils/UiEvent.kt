package com.section11.expenselens.ui.utils

abstract class UiEvent

/**
 * DownstreamUiEvent represents and event that goes from the ViewModel down to the UI
 */
abstract class DownstreamUiEvent : UiEvent() {
    data class Loading(val isLoading: Boolean) : DownstreamUiEvent()
    data class Error(val message: String?) : DownstreamUiEvent()
    data class ShowSnackBar(val message: String) : DownstreamUiEvent()
}

/**
 * UpstreamUiEvent represents and event that goes from the UI up to the ViewModel
 */
abstract class UpstreamUiEvent: UiEvent()
