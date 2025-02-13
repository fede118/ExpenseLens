package com.section11.expenselens.ui.utils

import androidx.compose.material3.SnackbarDuration

abstract class UiEvent

/**
 * DownstreamUiEvent represents and event that goes from the ViewModel down to the UI
 */
abstract class DownstreamUiEvent : UiEvent() {
    data class Loading(val isLoading: Boolean) : DownstreamUiEvent()
    data class Error(val message: String?) : DownstreamUiEvent()
    data class ShowSnackBar(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : DownstreamUiEvent()
}

/**
 * UpstreamUiEvent represents and event that goes from the UI up to the ViewModel
 */
abstract class UpstreamUiEvent: UiEvent()
