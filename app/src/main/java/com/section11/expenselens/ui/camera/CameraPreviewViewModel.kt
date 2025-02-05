package com.section11.expenselens.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.section11.expenselens.domain.usecase.ImageToTextUseCase
import com.section11.expenselens.domain.usecase.ExpenseInformationUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.ui.camera.CameraPreviewViewModel.CameraPreviewUiState.ShowCameraPreview
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.utils.UiState
import com.section11.expenselens.ui.utils.UiState.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraPreviewViewModel @Inject constructor(
    private val imageToTextUseCase: ImageToTextUseCase,
    private val expenseInformationUseCase: ExpenseInformationUseCase,
    private val navigationManager: NavigationManager,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(ShowCameraPreview)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onUiEvent(cameraPreviewEvent: CameraPreviewEvents) {
        when (cameraPreviewEvent) {
            is CameraPreviewEvents.OnCaptureImageTapped -> onImageCaptureTap()
        }
    }

    private fun onImageCaptureTap() {
        _uiState.value = Loading
        imageToTextUseCase.takePicture { result ->
            result.onSuccess { extractedText ->
                viewModelScope.launch(dispatcher) {
                    val expenseInfo = expenseInformationUseCase.getExpenseInfo(extractedText)
                    navigationManager.navigate(NavigateToExpensePreview(extractedText, expenseInfo))
                    _uiState.value = ShowCameraPreview
                }
            }
            result.onFailure { error ->
                _uiState.value = UiState.Error(error.message)
            }
        }
    }

    sealed class CameraPreviewUiState : UiState() {
        data object ShowCameraPreview : CameraPreviewUiState()
        data class Loading(val bitmap: Bitmap): CameraPreviewUiState()
    }
}
