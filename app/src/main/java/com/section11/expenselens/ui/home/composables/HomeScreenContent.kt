package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.ui.common.ExpenseLensLoader
import com.section11.expenselens.ui.common.ProfileDialog
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignOutTapped
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.theme.LocalSnackbarHostState
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import com.section11.expenselens.ui.utils.DownstreamUiEvent.ShowSnackBar
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val uiState by homeUiState.collectAsState()
    val dimens = LocalDimens.current
    var showDialog by remember { mutableStateOf(false) }

    HandleDownStreamEvents(downstreamUiEvent)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.m2) // Add some padding to avoid screen edges
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.m2)
                .fillMaxWidth()
        ) {
            val iconModifier = Modifier
                .size(dimens.m5)
                .align(Alignment.End)
                .clip(CircleShape)
            when (uiState) {
                is UserSignedIn -> {
                    val user = (uiState as UserSignedIn).user
                    ProfilePictureIcon(
                        user = user,
                        modifier = iconModifier.clickable { showDialog = true }
                    )

                    if (showDialog) {
                        ProfileDialog(
                            profileImageUrl = user.profilePic,
                            userName = user.displayName,
                            onDismiss = { showDialog = false },
                            onLogout = {
                                showDialog = false
                                onEvent(SignOutTapped)
                            }
                        )
                    }
                    Greeting((uiState as UserSignedIn).greeting)
                }
                is UserSignedOut -> {
                    GoogleSignInButton(iconModifier) { event ->
                        onEvent(event)
                    }
                    Greeting((uiState as UserSignedOut).greeting)
                }
            }

            Spacer(modifier = Modifier.padding(dimens.m2))
        }

        FloatingActionButton(
            onClick = { onEvent(AddExpenseTapped) },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(dimens.m2)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
        }
    }
}

@Composable
fun HandleDownStreamEvents(downstreamUiEvent: SharedFlow<DownstreamUiEvent>) {
    var isLoading by remember { mutableStateOf(false) }
    val uiEvent by downstreamUiEvent.collectAsState(null)
    val rememberCoroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(uiEvent) {
        when(uiEvent) {
            is Loading -> (uiEvent as? Loading)?.isLoading?.let { isLoading = it }
            is ShowSnackBar -> {
                (uiEvent as? ShowSnackBar)?.message?.let { message ->
                    rememberCoroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }

    if (isLoading) {
        ExpenseLensLoader(Modifier.fillMaxSize())
    }
}

@Composable
fun Greeting(greeting: String) {
    val dimens = LocalDimens.current
    Spacer(modifier = Modifier.padding(dimens.m3))
    Row {
        Text(
            modifier = Modifier
                .padding(horizontal = dimens.m2)
                .fillMaxWidth(),
            text = greeting,
            textAlign = TextAlign.Center,
        )
    }

}

@Composable
fun GoogleSignInButton(modifier: Modifier = Modifier, onEvent: (HomeUpstreamEvent) -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    Image(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = "Profile Picture",
        modifier = modifier.clickable { onEvent(SignInTapped(context)) },
        colorFilter = ColorFilter.tint(colorScheme.contentColorFor(colorScheme.background))
    )
}

@Composable
fun ProfilePictureIcon(user: UserInfoUiModel, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(user.profilePic),
        contentDescription = "Profile Picture",
        modifier = modifier
    )
}

@DarkAndLightPreviews
@Composable
fun HomeScreenNoSignInPreview() {
    ExpenseLensTheme {
        Surface {
            HomeScreenContent(
                modifier = Modifier.fillMaxSize(),
                homeUiState = MutableStateFlow(UserSignedOut("Test Greeting")),
                downstreamUiEvent = MutableSharedFlow()
            ) {}
        }
    }
}

@DarkAndLightPreviews
@Composable
fun HomeScreenSignedInPreview() {
    val userSignedIn = MutableStateFlow(
        UserSignedIn(
            "Hello, welcome to expense lens",
            UserInfoUiModel("id", "Test User", "")
        )
    )
    ExpenseLensTheme {
        Surface {
            HomeScreenContent(
                modifier = Modifier.fillMaxSize(),
                homeUiState = userSignedIn,
                downstreamUiEvent = MutableSharedFlow()
            ) {}
        }
    }
}
