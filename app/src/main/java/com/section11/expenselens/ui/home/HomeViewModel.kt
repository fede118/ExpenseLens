package com.section11.expenselens.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.models.ReceiptInformation
import com.section11.expenselens.domain.usecase.ReceiptInformationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val receiptInformationUseCase: ReceiptInformationUseCase,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onTextExtractedFromImage(extractedText: String) {
        viewModelScope.launch(dispatcher) {
            val receiptInformation = receiptInformationUseCase.getReceiptInfo(extractedText)
            _uiState.value = HomeUiState.TextExtractedFromImage(extractedText, receiptInformation)
        }
    }

    sealed class HomeUiState {
        data object Idle : HomeUiState()
        data class TextExtractedFromImage(
            val extractedText: String,
            val processedTextByGemini: ReceiptInformation
        ): HomeUiState()
    }
}
