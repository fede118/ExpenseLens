package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.section11.expenselens.ui.camera.composables.CameraScreenContent
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CameraRoute(
    cameraPreviewUiState: StateFlow<UiState>,
    modifier: Modifier = Modifier,
    onEvent: (CameraPreviewEvents) -> Unit
) {
    CameraScreenContent(cameraPreviewUiState, modifier, onEvent)
}
