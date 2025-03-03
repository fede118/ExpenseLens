package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.ContentWithBadge
import com.section11.expenselens.ui.common.MaxCharsOutlinedTextField
import com.section11.expenselens.ui.common.ProfileDialog
import com.section11.expenselens.ui.common.ProfilePictureIcon
import com.section11.expenselens.ui.common.TransformingButton
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdLoading
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.HouseholdInviteResultEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.AddUserToHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToExpensesHistoryTapped
import com.section11.expenselens.ui.home.model.PendingInvitesUiModel
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

private const val HOUSEHOLD_NAME_MAX_CHARS = 25

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
fun ColumnScope.SignedInGreetingAndIcon(
    userInfo: UserInfoUiModel,
    hasHousehold: Boolean,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    greeting: String,
    onEvent: (HomeUpstreamEvent) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = LocalDimens.current
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContentWithBadge(showBadge = userInfo.pendingInvites.isNotEmpty()) {
            ProfilePictureIcon(
                user = userInfo,
                modifier = modifier
                    .size(dimens.m5)
                    .clip(CircleShape)
                    .clickable { showDialog = true }
            )
        }
    }

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
            ProfileDialogContent(userInfo, hasHousehold, dialogDownstreamUiEvent, onEvent)
        }
    }
}

@Composable
fun ProfileDialogContent(
    userInfo: UserInfoUiModel,
    hasHousehold: Boolean,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current
    val dialogUiEvent by dialogDownstreamUiEvent.collectAsState(null)
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
            is HouseholdInviteResultEvent -> {
                isAddUsersLoading = false
                invitationResultMessage = (dialogUiEvent as HouseholdInviteResultEvent).message
                invitationResultMessageColor = (dialogUiEvent as HouseholdInviteResultEvent).textColor
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (hasHousehold) {
            Button(
                onClick = { onEvent(ToExpensesHistoryTapped) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.home_screen_expense_history_label))
            }

            TransformingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimens.m1),
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

            Spacer(Modifier.height(dimens.m1))
        }

        PendingInvitesSection(userInfo.pendingInvites, userInfo.id, onEvent)

        Spacer(Modifier.height(dimens.m1))
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
    ) {
        SignedInGreetingAndIcon(userInfo, true, dialogDownstreamUiEvent, greeting, onEvent) {
            Text(stringResource(R.string.home_screen_household_name_prefix, household.name))
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

    BoxHomeScreenContainer {
        SignedInGreetingAndIcon(userInfo, false, dialogDownstreamUiEvent, greeting, onEvent) {
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
fun PendingInvitesSection(
    pendingInvitesUiModel: List<PendingInvitesUiModel>,
    userId: String,
    onEvent: (HomeUpstreamEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalDimens.current

    if (pendingInvitesUiModel.isNotEmpty()) {
        Column(
            modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            pendingInvitesUiModel.forEach {
                if (it.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(dimens.m5))
                } else {
                    PendingInviteItem(it, userId, onEvent)
                }
            }
        }
    }
}


@Composable
fun PendingInviteItem(
    pendingInvite: PendingInvitesUiModel,
    userId: String,
    onEvent: (HomeUpstreamEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth(1f),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.profile_dialog_invitation_prefix, pendingInvite.householdName),
            style = MaterialTheme.typography.bodySmall
        )
    }
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = {
                onEvent(HouseholdInviteTap(
                    pendingInvite.inviteId,
                    pendingInvite.householdId,
                    pendingInvite.householdName,
                    userId,
                    true
                ))
            }
        ) {
            Text(
                stringResource(R.string.profile_dialog_accept_invitation),
                style = MaterialTheme.typography.bodySmall
            )
        }
        TextButton(
            onClick = {
                onEvent(HouseholdInviteTap(
                    pendingInvite.inviteId,
                    pendingInvite.householdId,
                    pendingInvite.householdName,
                    userId,
                    false
                ))
            }
        ) {
            Text(
                stringResource(R.string.profile_dialog_reject_invitation),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@DarkAndLightPreviews
@Composable
fun SignedInWithHouseholdPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        HomeScreenContent(
            homeUiStateFlow = MutableStateFlow(fakeRepo.getUserSignedInState()),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}

@DarkAndLightPreviews
@Composable
fun SignedInPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        HomeScreenContent(
            homeUiStateFlow =  MutableStateFlow(fakeRepo.getUserSignedInState()),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}
