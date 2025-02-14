package com.section11.expenselens.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun HandleDownstreamEvents(
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    val uiEvent by downstreamUiEvent.collectAsState(null)

    LaunchedEffect(uiEvent) {
        when(uiEvent) {
            is Loading -> (uiEvent as? Loading)?.isLoading?.let { isLoading = it }
        }
    }

    if (isLoading) {
        ExpenseLensLoader(modifier.fillMaxSize())
    }
}
