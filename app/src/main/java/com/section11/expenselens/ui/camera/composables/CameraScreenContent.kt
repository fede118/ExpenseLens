package com.section11.expenselens.ui.camera.composables

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.section11.expenselens.ui.navigation.NavigationEvent
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreenContent(
    modifier: Modifier = Modifier,
    onNavigationEvent: (NavigationEvent) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        FullScreenCameraView(modifier.fillMaxSize()) { extractedText ->
            onNavigationEvent(NavigationEvent.TextExtractedFromImage(extractedText))
            Log.d("Camera", "Image captured: $extractedText")
        }
    } else {
        RequestCameraPermission(cameraPermissionState)
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestCameraPermission(cameraPermissionState: PermissionState) {
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Suppress("SwallowedException") // TODO: I need to create an event class to return errors
@Composable
fun FullScreenCameraView(modifier: Modifier = Modifier, onTextExtracted: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER // Ensure proper scaling
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider) // Ensure this is set
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: UnsupportedOperationException) {
                /* no op */
            }
        }
    )

    CaptureImageButton(
        imageCapture,
        context,
        onImageCaptured = { bitmap ->
            processImage(bitmap) { extractedText ->
                onTextExtracted(extractedText)
            }
            Log.e("Camera", "Image captured")
        }
    ) { onError ->
        Log.e("Camera", "Image capture failed", onError)
    }
}

@Composable
fun CaptureImageButton(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    val dimens = LocalDimens.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        FloatingActionButton(
            onClick = {
                val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            onImageCaptured(bitmap)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onError(exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(dimens.m2),
            containerColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Capture Image",
                tint = Color.Black
            )
        }
    }
}

private fun processImage(bitmap: Bitmap, onResult: (String) -> Unit) {
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder()
            .setExecutor(Executors.newSingleThreadExecutor())
            .build()
    )

    recognizer.process(inputImage)
        .addOnSuccessListener { visionText ->
            val extractedText = visionText.text
            onResult(extractedText)
        }
        .addOnFailureListener { e ->
            // TODO: do more than Log the error
            Log.e("MLKit", "Text recognition failed", e)
        }
}

@DarkAndLightPreviews
@Composable
fun CameraScreenContentPreview() {
    ExpenseLensTheme {
        Surface {
            CameraScreenContent(onNavigationEvent = {})
        }
    }
}
