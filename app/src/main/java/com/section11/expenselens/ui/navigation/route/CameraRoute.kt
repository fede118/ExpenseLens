package com.section11.expenselens.ui.navigation.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.section11.expenselens.ui.camera.composables.CameraScreenContent
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun CameraRoute(
    modifier: Modifier = Modifier,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (CameraPreviewEvents) -> Unit
) {
    CameraScreenContent(modifier, downstreamUiEvent, onEvent)
}
