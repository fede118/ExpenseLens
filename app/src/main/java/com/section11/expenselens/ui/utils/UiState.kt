package com.section11.expenselens.ui.utils

abstract class UiState {
    data object Loading : UiState()
    data object Idle : UiState()
    data class Error(val message: String?) : UiState()
}
