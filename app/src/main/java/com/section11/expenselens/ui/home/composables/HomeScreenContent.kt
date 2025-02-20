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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.HandleDownstreamEvents
import com.section11.expenselens.ui.common.MaxCharsOutlinedTextField
import com.section11.expenselens.ui.common.ProfileDialog
import com.section11.expenselens.ui.common.ProfilePictureIcon
import com.section11.expenselens.ui.common.TransformingButton
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedOut
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdLoading
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdResult
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.AddUserToHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToExpensesHistoryTapped
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

private const val HOUSEHOLD_NAME_MAX_CHARS = 25

@Composable
fun HomeScreenContent(
    homeUiStateFlow: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit = {}
) {
    val uiState by homeUiStateFlow.collectAsState()
    when (uiState) {
        is UserSignedIn -> SignedInUi(uiState as UserSignedIn, dialogDownstreamUiEvent, onEvent)
        is UserSignedOut -> LoggedOutUi(uiState as UserSignedOut, onEvent)
    }

    HandleDownstreamEvents(downstreamUiEvent)
}

@Composable
fun SignedInUi(
    userSignedInModel: UserSignedIn,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    with(userSignedInModel) {
        if (householdInfo != null) {
            ExistingHouseholdUi(
                user,
                dialogDownstreamUiEvent,
                greeting,
                householdInfo,
                onEvent,
            )
        } else {
            CreateHouseholdUi(
                Modifier.fillMaxWidth(1f),
                user,
                dialogDownstreamUiEvent,
                greeting,
                onEvent
            )
        }
    }
}

@Composable
fun ExistingHouseholdUi(
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    greeting: String,
    household: HouseholdUiState,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current
    BoxHomeScreenContainer(
        boxContent = {
            FloatingActionButton(
                onClick = { onEvent(AddExpenseTapped) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimens.m2),

                ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_screen_add_expense_label)
                )
            }
        }
    ) { iconModifier ->
        SignedInGreetingAndIcon(iconModifier, userInfo, dialogDownstreamUiEvent, greeting, onEvent) {
            Text(stringResource(R.string.home_screen_household_name_prefix, household.name))
        }
    }
}

@Composable
fun BoxHomeScreenContainer(
    modifier: Modifier = Modifier,
    boxContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.(iconModifier: Modifier) -> Unit
) {
    val dimens = LocalDimens.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.m2)
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.m2)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconModifier = Modifier
                .size(dimens.m5)
                .align(Alignment.End)
                .clip(CircleShape)

            content(iconModifier)
        }
        boxContent()
    }
}

@Composable
fun ColumnScope.SignedInGreetingAndIcon(
    modifier: Modifier = Modifier,
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    greeting: String,
    onEvent: (HomeUpstreamEvent) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val dialogUiEvent by dialogDownstreamUiEvent.collectAsState(null)
    var showDialog by remember { mutableStateOf(false) }
    var isAddUsersLoading by remember { mutableStateOf(false) }
    var invitationResultMessage: String? by remember { mutableStateOf(null) }
    var invitationResultMessageColor: Color? by remember { mutableStateOf(null) }

    LaunchedEffect(dialogUiEvent) {
        when(dialogUiEvent) {
            is AddUserToHouseholdLoading -> {
                (dialogUiEvent as AddUserToHouseholdLoading).isLoading.let {
                    isAddUsersLoading = it
                }
            }
            is AddUserToHouseholdResult -> {
                isAddUsersLoading = false
                invitationResultMessage = (dialogUiEvent as AddUserToHouseholdResult).message
                invitationResultMessageColor = (dialogUiEvent as AddUserToHouseholdResult).textColor
            }
        }
    }

    ProfilePictureIcon(
        user = userInfo,
        modifier = modifier.clickable { showDialog = true }
    )

    Greeting(greeting)

    content()

    if (showDialog) {
        ProfileDialog(
            profileImageUrl = userInfo.profilePic,
            userName = userInfo.displayName,
            onDismiss = { showDialog = false },
            onLogout = {
                showDialog = false
                onEvent(SignOutTapped)
            }
        ) {
            Column {
                Button(
                    onClick = { onEvent(ToExpensesHistoryTapped) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.home_screen_expense_history_label))
                }

                TransformingButton(
                    buttonLabel = stringResource(R.string.home_screen_invite_to_household_label),
                    isLoading = isAddUsersLoading,
                    placeHolderText = stringResource(R.string.home_screen_invite_to_household_placeholder),
                    supportingText = {
                        invitationResultMessage?.let {
                            Text(it, color = invitationResultMessageColor ?: Color.Unspecified)
                        }
                    }
                ) { enteredText ->
                    onEvent(AddUserToHouseholdTapped(userInfo.id, enteredText))
                }
            }
        }
    }
}

@Composable
fun CreateHouseholdUi(
    modifier: Modifier = Modifier,
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    greeting: String,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    var text by remember { mutableStateOf(String()) }
    val dimens = LocalDimens.current

    BoxHomeScreenContainer { iconModifier ->
        SignedInGreetingAndIcon(iconModifier, userInfo, dialogDownstreamUiEvent, greeting, onEvent) {
            Spacer(Modifier.height(dimens.m1))
            Text(
                stringResource(R.string.home_screen_no_household_message)
            )
            Spacer(Modifier.height(dimens.m1))
            MaxCharsOutlinedTextField(
                modifier = modifier,
                value = text,
                onValueChange = { text = it },
                title = stringResource(R.string.home_screen_create_household_name),
                maxLength = HOUSEHOLD_NAME_MAX_CHARS
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onEvent(CreateHouseholdTapped(userInfo.id, text))
                }
            ) {
                Text(stringResource(R.string.home_screen_create_household))
            }
        }
    }
}

@Composable
fun LoggedOutUi(state: UserSignedOut, onEvent: (HomeUpstreamEvent) -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    BoxHomeScreenContainer { iconModifier ->
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.home_screen_profile_pic_content_description),
            modifier = iconModifier.clickable { onEvent(SignInTapped(context)) },
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

@DarkAndLightPreviews
@Composable
fun SignedInPreview() {
    val userSignedIn = MutableStateFlow(
        UserSignedIn(
            "Hello, welcome to expense lens",
            UserInfoUiModel("id", "Test User", "")
        )
    )
    Preview {
        HomeScreenContent(
            homeUiStateFlow = userSignedIn,
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}

@DarkAndLightPreviews
@Composable
fun SignedInWithHouseholdPreview() {
    val userSignedIn = MutableStateFlow(
        UserSignedIn(
            "Hello, welcome to expense lens",
            UserInfoUiModel("id", "Test User", ""),
            HouseholdUiState("id", "testing")
        )
    )
    Preview {
        HomeScreenContent(
            homeUiStateFlow = userSignedIn,
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}
