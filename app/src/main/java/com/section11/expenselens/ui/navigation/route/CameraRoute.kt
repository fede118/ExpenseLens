package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.section11.expenselens.ui.camera.CameraPreviewViewModel
import com.section11.expenselens.ui.camera.composables.CameraScreenContent

@Composable
fun CameraRoute(modifier: Modifier = Modifier) {
    val cameraPreviewViewModel = hiltViewModel<CameraPreviewViewModel>()

    CameraScreenContent(
        downstreamUiEvent = cameraPreviewViewModel.uiEvent,
        onUiEvent = cameraPreviewViewModel::onUiEvent,
        modifier
    )
}
