package com.section11.expenselens.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.usecase.ExpenseInformationUseCase
import com.section11.expenselens.domain.usecase.ImageToTextUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents.OnCaptureImageTapped
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Error
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraPreviewViewModel @Inject constructor(
    private val imageToTextUseCase: ImageToTextUseCase,
    private val expenseInformationUseCase: ExpenseInformationUseCase,
    private val navigationManager: NavigationManager,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<DownstreamUiEvent>()
    val uiEvent: SharedFlow<DownstreamUiEvent> = _uiEvent

    fun onUiEvent(cameraPreviewEvent: CameraPreviewEvents) {
        when (cameraPreviewEvent) {
            is OnCaptureImageTapped -> onImageCaptureTap()
        }
    }

    private fun onImageCaptureTap() {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(Loading(true))
        }
        imageToTextUseCase.takePicture { result ->
            viewModelScope.launch(dispatcher) {
                result.onSuccess { extractedText ->
                    val expenseResult = expenseInformationUseCase.getExpenseInfo(extractedText)
                    handleExpenseResult(extractedText, expenseResult)
                }
                result.onFailure { error -> _uiEvent.emit(Error(error.message)) }
                _uiEvent.emit(Loading(false))
            }
        }
    }

    private suspend fun handleExpenseResult(
        extractedText: String,
        expenseResult: Result<SuggestedExpenseInformation>
    ) {
        with(expenseResult) {
            onSuccess { expense ->
                navigationManager.navigate(NavigateToExpensePreview(extractedText, expense))
            }
            onFailure { error -> _uiEvent.emit(Error(error.message)) }
        }
    }
}
