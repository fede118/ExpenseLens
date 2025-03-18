package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.HandleDownstreamEvents
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreenContent(
    homeUiStateFlow: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit = {}
) {
    val uiState by homeUiStateFlow.collectAsState()
    HandleDownstreamEvents(downstreamUiEvent, initialState = Loading(uiState == UiState.Idle))

    when (uiState) {
        is UserSignedIn -> SignedInUi(uiState as UserSignedIn, dialogDownstreamUiEvent, onEvent)
        is UserSignedOut -> LoggedOutUi(uiState as UserSignedOut, onEvent)
    }
}

@Composable
fun BoxHomeScreenContainer(
    modifier: Modifier = Modifier,
    boxContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = LocalDimens.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(dimens.m2)
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.m2)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
        boxContent()
    }
}

@Composable
fun LoggedOutUi(state: UserSignedOut, onEvent: (HomeUpstreamEvent) -> Unit) {
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    BoxHomeScreenContainer {
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.content_description_profile_pic),
            modifier = Modifier
                .size(dimens.m5)
                .align(Alignment.End)
                .clip(CircleShape)
                .clickable { onEvent(SignInTapped(context)) },
            colorFilter = ColorFilter.tint(colorScheme.contentColorFor(colorScheme.background))
        )
        Greeting(state.greeting)
    }
}

@Composable
fun Greeting(greeting: String) {
    val dimens = LocalDimens.current
    Spacer(modifier = Modifier.padding(dimens.m3))
    Column {
        Text(
            modifier = Modifier
                .padding(horizontal = dimens.m2)
                .fillMaxWidth(),
            text = greeting,
            textAlign = TextAlign.Center,
        )
    }
}

@DarkAndLightPreviews
@Composable
fun LoggedOutPreview() {
    Preview {
        HomeScreenContent(
            homeUiStateFlow = MutableStateFlow(UserSignedOut("Test Greeting")),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        ) {}
    }
}
