package com.section11.expenselens.ui.camera.event

import com.section11.expenselens.ui.utils.UiEvent

open class CameraPreviewEvents : UiEvent() {
    data object OnCaptureImageTapped : CameraPreviewEvents()
    data class OnImageCaptureError(val message: String?) : CameraPreviewEvents()
}
