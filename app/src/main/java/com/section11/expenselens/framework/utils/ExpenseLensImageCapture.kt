package com.section11.expenselens.framework.utils

import androidx.camera.core.ImageCapture
import java.util.concurrent.Executor
import javax.inject.Inject

interface ImageCaptureWrapper {
    fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        executor: Executor,
        imageSavedCallback: ImageCapture.OnImageSavedCallback
    )
}

/**
 * This class only serves the purpose of abstracting the image capture for testing.
 * Since the [ImageCapture] is Java FINAL class it cannot be mocked. So I created this wrapper in
 * order to mock it
 */
class ExpenseLensImageCapture @Inject constructor(
    private val imageCapture: ImageCapture
): ImageCaptureWrapper {
    override fun takePicture(
        outputFileOptions: ImageCapture.OutputFileOptions,
        executor: Executor,
        imageSavedCallback: ImageCapture.OnImageSavedCallback
    ) {
        return imageCapture.takePicture(outputFileOptions, executor, imageSavedCallback)
    }
}
