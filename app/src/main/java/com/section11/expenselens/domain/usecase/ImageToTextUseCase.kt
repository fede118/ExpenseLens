package com.section11.expenselens.domain.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.section11.expenselens.framework.utils.ImageCaptureWrapper
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Inject

private const val ZERO_ROTATION_DEGREES = 0
private const val DOT_JPEG = ".jpg"

class ImageToTextUseCase @Inject constructor(
    private val externalCacheDir: File?,
    private val mainExecutor: Executor,
    private val imageCapture: ImageCaptureWrapper,
    private val textRecognizer: TextRecognizer
) {

    fun takePicture(onResult: (Result<String>) -> Unit) {
        val file = File(externalCacheDir, System.currentTimeMillis().toString() + DOT_JPEG)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            ImageCaptureCallback(file) { result: Result<Bitmap> ->
                result.onSuccess {
                    processImage(it) { textExtractionResult ->
                        onResult(textExtractionResult)
                    }
                }
                result.onFailure { error -> onResult(Result.failure(error)) }
            }
        )
    }


    internal class ImageCaptureCallback(
        private val file: File,
        private val onResult: (Result<Bitmap>) -> Unit
    ) : ImageCapture.OnImageSavedCallback {

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            onResult(Result.success(bitmap))
        }

        override fun onError(exception: ImageCaptureException) {
            onResult(Result.failure(exception))
        }
    }

    private fun processImage(bitmap: Bitmap, onResult: (Result<String>) -> Unit) {
        val inputImage = InputImage.fromBitmap(bitmap, ZERO_ROTATION_DEGREES)

        textRecognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                onResult(Result.success(extractedText))
            }
            .addOnFailureListener { error -> onResult(Result.failure(error)) }
    }
}
