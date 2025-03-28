package com.section11.expenselens.ui.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.AnimateFromBottom
import com.section11.expenselens.ui.common.BoxedColumnFullScreenContainer
import com.section11.expenselens.ui.common.ContentWithBadge
import com.section11.expenselens.ui.common.LabeledIcon
import com.section11.expenselens.ui.common.MaxCharsOutlinedTextField
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.UserSignedIn.HouseholdUiState
import com.section11.expenselens.ui.home.dialog.ProfileDialog
import com.section11.expenselens.ui.home.dialog.ProfileDialogContent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.CreateHouseholdTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.HouseholdInviteTap
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.JoinHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.SignOutTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToHouseholdDetailsTapped
import com.section11.expenselens.ui.home.model.NoHouseholdInputMode
import com.section11.expenselens.ui.home.model.NoHouseholdInputMode.Create
import com.section11.expenselens.ui.home.model.NoHouseholdInputMode.Join
import com.section11.expenselens.ui.home.model.NoHouseholdInputMode.None
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
private const val CAKE_GRAPH_SIZE = 0.4f

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
                householdInfo,
                onEvent,
            )
        } else {
            NoHouseholdUi(
                Modifier.fillMaxWidth(1f),
                user,
                dialogDownstreamUiEvent,
                onEvent
            )
        }
    }
}

@Composable
fun SignedInIcon(
    userInfo: UserInfoUiModel,
    hasHousehold: Boolean,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContentWithBadge(showBadge = userInfo.pendingInvites.isNotEmpty()) {
            LabeledIcon(
                painterResource = rememberAsyncImagePainter(userInfo.profilePic),
                label = userInfo.displayName.orEmpty(),
                iconSize = LocalDimens.current.m6,
                contentDescription = stringResource(R.string.content_description_profile_pic),
                modifier = modifier,
                colorFilter = null,
            ) { showDialog = true }
        }
    }

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
fun ExistingHouseholdUi(
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    household: HouseholdUiState,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current
    BoxedColumnFullScreenContainer(
        columnVerticalArrengement = Arrangement.SpaceEvenly
    ) {
        Row(Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.Center) {
            SignedInIcon(userInfo, true, dialogDownstreamUiEvent, onEvent)
            Spacer(Modifier.width(dimens.m5))
            LabeledIcon(
                painterResource = painterResource(R.drawable.house_icon),
                label = household.name,
                contentDescription = stringResource(R.string.home_screen_my_household_label),
            ) { onEvent(ToHouseholdDetailsTapped) }
        }
        household.graphInfo?.let {
            CakeGraph(
                graphUiModel = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surfaceContainer)
                    .padding(dimens.m2)
                    .fillMaxHeight(CAKE_GRAPH_SIZE)
            )
        }
        AddExpenseButtons(
            modifier = Modifier.padding(bottom = dimens.m2).fillMaxWidth(1f),
            onEvent = onEvent
        )
    }
}

@Composable
fun NoHouseholdUi(
    modifier: Modifier = Modifier,
    userInfo: UserInfoUiModel,
    dialogDownstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onEvent: (HomeUpstreamEvent) -> Unit
) {
    val inputMode = remember { mutableStateOf<NoHouseholdInputMode>(None) }
    val dimens = LocalDimens.current

    BackHandler(inputMode.value != None) { inputMode.value = None }

    BoxedColumnFullScreenContainer(
        boxContent = {
            AnimateFromBottom(inputMode.value == Create) {
                CreateHouseholdInput { input ->
                    onEvent(CreateHouseholdTapped(userInfo.id, input))
                }
            }
            AnimateFromBottom(inputMode.value == Join) {
                JoinHouseholdInput { input ->
                    onEvent(JoinHouseholdTapped(userInfo.id, input))
                }
            }
        }
    ) {
        Spacer(Modifier.height(dimens.m11))
        SignedInIcon(userInfo, false, dialogDownstreamUiEvent, onEvent)

        Spacer(Modifier.height(dimens.m11))
        Row(modifier, horizontalArrangement = Arrangement.Center) {
            LabeledIcon(
                painterResource = painterResource(R.drawable.house_icon),
                label = stringResource(R.string.home_screen_create_household),
            ) { inputMode.value = Create }

            Spacer(Modifier.width(dimens.m4))

            LabeledIcon(
                painterResource = painterResource(R.drawable.house_icon),
                label = stringResource(R.string.home_screen_join_household)
            ) { inputMode.value = Join }
        }
    }
}

@Composable
fun CreateHouseholdInput(modifier: Modifier = Modifier, onButtonTap: (String) -> Unit) {
    val householdName = stringResource(R.string.home_screen_create_household_name)
    OutlinedTextFieldAndButtonComponent(
        modifier = modifier,
        title = householdName,
        buttonText = stringResource(R.string.home_screen_create_household_button),
        onButtonTap = { text -> onButtonTap(text) },
        leadingIcon = { Image(painterResource(R.drawable.house_icon), householdName) }
    )
}

@Composable
fun JoinHouseholdInput(modifier: Modifier = Modifier, onButtonTap: (String) -> Unit) {
    val joinHousehold = stringResource(R.string.home_screen_join_household)
    OutlinedTextFieldAndButtonComponent(
        modifier = modifier,
        title = joinHousehold,
        buttonText = stringResource(R.string.home_screen_join_button),
        onButtonTap = { text -> onButtonTap(text) },
        leadingIcon = {
            Image(
                rememberVectorPainter(Icons.Default.Email),
                joinHousehold,
                colorFilter = ColorFilter.tint(colorScheme.contentColorFor(colorScheme.background))
            )
        }
    )
}

@Composable
fun OutlinedTextFieldAndButtonComponent(
    title: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable () -> Unit,
    onButtonTap: (String) -> Unit
) {
    var text by remember { mutableStateOf(String()) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MaxCharsOutlinedTextField(
            value = text,
            onValueChange = { text = it },
            title = title,
            maxLength = HOUSEHOLD_NAME_MAX_CHARS,
            leadingIcon = { leadingIcon() }
        )
        Button(
            modifier = Modifier.width(LocalDimens.current.m24),
            onClick = { onButtonTap(text) },
            colors = ButtonDefaults.textButtonColors().copy(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(buttonText, color = MaterialTheme.colorScheme.surface)
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
fun SignedInNoHouseholdPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        HomeScreenContent(
            homeUiStateFlow =  MutableStateFlow(fakeRepo.getUserSignedInState(false)),
            dialogDownstreamUiEvent = MutableSharedFlow(),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}
