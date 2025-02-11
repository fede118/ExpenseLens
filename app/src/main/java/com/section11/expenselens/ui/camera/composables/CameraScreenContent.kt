package com.section11.expenselens.ui.camera.composables

import android.Manifest
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.section11.expenselens.framework.di.ImageCaptureEntryPoint
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents.OnCaptureImageTapped
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents.OnImageCaptureError
import com.section11.expenselens.ui.common.BlurredBackgroundExpenseLensLoader
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreenContent(
    modifier: Modifier = Modifier,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onUiEvent: (CameraPreviewEvents) -> Unit
) {
    val appContext = LocalContext.current.applicationContext
    val imageCapture = remember { EntryPointAccessors.fromApplication(
            context = appContext,
            entryPoint = ImageCaptureEntryPoint::class.java
        ).getImageCapture()
    }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    HandleDownStreamEvents(downstreamUiEvent)

    if (cameraPermissionState.status.isGranted) {
        FullScreenCameraView(
            modifier = modifier.fillMaxSize(),
            imageCapture = imageCapture
        ) { onEvent -> onUiEvent(onEvent) }
    } else {
        RequestCameraPermission(cameraPermissionState)
    }
}


@Composable
fun HandleDownStreamEvents(downstreamUiEvent: SharedFlow<DownstreamUiEvent>) {
    val uiEvent by downstreamUiEvent.collectAsState(null)
    var isLoading by remember { mutableStateOf(false) }
    LaunchedEffect(uiEvent) {
        when(uiEvent) {
            is Loading -> (uiEvent as? Loading)?.isLoading?.let { isLoading = it
            }
        }
    }

    if (isLoading) {
        BlurredBackgroundExpenseLensLoader()
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

@Composable
fun FullScreenCameraView(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture,
    onEvent: (CameraPreviewEvents) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { androidViewContext ->
            PreviewView(androidViewContext).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
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
            } catch (exception: UnsupportedOperationException) {
                onEvent(OnImageCaptureError(exception.message))
            }
        }
    )

    CaptureImageButton { event -> onEvent(event) }
}

@Composable
fun CaptureImageButton(onEvent: (CameraPreviewEvents) -> Unit) {
    val dimens = LocalDimens.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        FloatingActionButton(
            onClick = { onEvent(OnCaptureImageTapped) },
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

@DarkAndLightPreviews
@Composable
fun CameraScreenContentPreview() {
    ExpenseLensTheme {
        Surface {
            CameraScreenContent(downstreamUiEvent = MutableSharedFlow()) {}
        }
    }
}
