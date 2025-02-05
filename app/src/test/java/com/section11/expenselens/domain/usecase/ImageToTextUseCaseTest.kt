package com.section11.expenselens.domain.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.section11.expenselens.framework.utils.ImageCaptureWrapper
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.concurrent.Executor

@RunWith(MockitoJUnitRunner::class)
class ImageToTextUseCaseTest {

    private val imageCapture: ImageCaptureWrapper = mock()
    private val mockBitmap: Bitmap = mock()
    private val imageCaptureCallbackCaptor = argumentCaptor<ImageCapture.OnImageSavedCallback>()
    // Im not able to mock this. even if I return a path it says is null
    private val externalCacheDir = File("/mock/cache/dir")
    private val mainExecutor: Executor = mock()
    private val textRecognizer: TextRecognizer = mock()

    private lateinit var imageToTextUseCase: ImageToTextUseCase

    @Before
    fun setUp() {
        mockkStatic("android.graphics.BitmapFactory")
        every { BitmapFactory.decodeFile(any()) } returns mockBitmap
        mockkStatic("com.google.mlkit.vision.common.InputImage")
        every { InputImage.fromBitmap(any(), any()) } returns mock()

        imageToTextUseCase = ImageToTextUseCase(
            externalCacheDir,
            mainExecutor,
            imageCapture,
            textRecognizer
        )
    }

    @Test
    fun `takePicture should process image and return text on success`() {
        // Given
        val mockTask: Task<Text> = mock()
        val mockText: Text = mock()
        whenever(mockText.text).thenReturn("text")
        whenever(textRecognizer.process(any<InputImage>())).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<Text>>(0)
            listener.onSuccess(mockText)
            mockTask
        }

        // When
        imageToTextUseCase.takePicture { result ->
            assert(result.isSuccess)
            assert(result.getOrNull() == mockText.text)
        }

        // Then
        verify(imageCapture).takePicture(
            any<ImageCapture.OutputFileOptions>(),
            eq(mainExecutor),
            imageCaptureCallbackCaptor.capture()
        )

        // Simulate image capture success
        val imageCaptureMock: ImageCapture.OutputFileResults = mock()
        imageCaptureCallbackCaptor.firstValue.onImageSaved(imageCaptureMock)

        // Verify text recognition process
        verify(textRecognizer).process(any<InputImage>())
    }

    @Test
    fun `takePicture should return failure if image capture fails`() {
        // Given
        val mockException = ImageCaptureException(0, "Capture failed", Throwable())

        // When
        imageToTextUseCase.takePicture { result ->
            assert(result.isFailure)
            assert(result.exceptionOrNull() == mockException)
        }

        // Then
        verify(imageCapture).takePicture(
            any<ImageCapture.OutputFileOptions>(),
            eq(mainExecutor),
            imageCaptureCallbackCaptor.capture()
        )

        // Simulate image capture failure
        imageCaptureCallbackCaptor.firstValue.onError(mockException)
    }

    @Test
    fun `takePicture should return failure if text recognition fails`() {
        // Given
        val mockException = Exception("Text recognition failed")

        val mockTask: Task<Text> = mock()
        whenever(textRecognizer.process(any<InputImage>())).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenReturn(mock())

        // When
        imageToTextUseCase.takePicture { result ->
            assert(result.isFailure)
            assert(result.exceptionOrNull() == mockException)
        }

        // Then
        verify(imageCapture).takePicture(
            any<ImageCapture.OutputFileOptions>(),
            eq(mainExecutor),
            imageCaptureCallbackCaptor.capture()
        )

        // Simulate image capture success
        imageCaptureCallbackCaptor.firstValue.onImageSaved(mock(ImageCapture.OutputFileResults::class.java))

        // Verify text recognition process
        verify(textRecognizer).process(any<InputImage>())
    }
}
