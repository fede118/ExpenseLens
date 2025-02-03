package com.section11.expenselens.ui.navigation

import androidx.compose.runtime.Composable
import com.section11.expenselens.ui.camera.composables.CameraScreenContent

@Composable
fun CameraRoute(onNavigationEvent: (NavigationEvent) -> Unit) {
    CameraScreenContent { navEvent ->
        onNavigationEvent(navEvent)
    }
}
