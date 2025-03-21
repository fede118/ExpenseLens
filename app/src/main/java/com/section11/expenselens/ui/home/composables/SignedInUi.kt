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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.BoxFullScreenContainer
import com.section11.expenselens.ui.common.ContentWithBadge
import com.section11.expenselens.ui.common.MaxCharsOutlinedTextField
import com.section11.expenselens.ui.common.ProfilePictureIcon
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.dialog.ProfileDialog
import com.section11.expenselens.ui.home.dialog.ProfileDialogContent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
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

@Composable
fun ExistingHouseholdUi(
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    greeting: String,
    household: HouseholdUiState,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current
    BoxFullScreenContainer(
        boxContent = { AddExpenseButton(onEvent = onEvent) }
    ) {
        SignedInGreetingAndIcon(userInfo, true, dialogDownstreamUiEvent, greeting, onEvent) {
            Text(stringResource(R.string.home_screen_household_name_prefix, household.name))
            Spacer(Modifier.height(dimens.m1))
            household.graphInfo?.let {
                CakeGraph(
                    graphUiModel = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimens.m40)
                )
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

    BoxFullScreenContainer {
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
