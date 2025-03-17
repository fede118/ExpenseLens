package com.section11.expenselens.ui.home.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.BuildConfig.VERSION_CODE
import com.section11.expenselens.BuildConfig.VERSION_NAME
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.CardDialog
import com.section11.expenselens.ui.common.TransformingButton
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.home.composables.PendingInvitesSection
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.AddUserToHouseholdLoading
import com.section11.expenselens.ui.home.dialog.DialogUiEvent.HouseholdInviteResultEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.AddUserToHouseholdTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToExpensesHistoryTapped
import com.section11.expenselens.ui.home.event.ProfileDialogEvents.ToHouseholdDetailsTapped
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ProfileDialog(
    modifier: Modifier = Modifier,
    profileImageUrl: String?,
    userName: String?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val dimens = LocalDimens.current

    CardDialog(modifier, onDismiss = onDismiss) {
        val shape = RoundedCornerShape(dimens.m5)
        Image(
            painter = rememberAsyncImagePainter(profileImageUrl),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(dimens.m10)
                .background(Color.Gray, shape = shape)
                .clip(shape)
        )

        Spacer(modifier = Modifier.height(dimens.m2))

        userName?.let { Text(text = it, style = MaterialTheme.typography.headlineSmall) }

        Spacer(modifier = Modifier.height(dimens.m2))

        content()

        // Logout Button
        Button(onClick = { onLogout() }) {
            Text(stringResource(R.string.profile_dialog_sign_out))
        }
        Spacer(modifier = Modifier.height(dimens.m2))
        Text(
            text = stringResource(R.string.profile_dialog_app_info, VERSION_NAME, VERSION_CODE),
            style = MaterialTheme.typography.labelSmall,
        )
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
        when(val event = dialogUiEvent) {
            is AddUserToHouseholdLoading -> isAddUsersLoading = event.isLoading
            is HouseholdInviteResultEvent -> {
                isAddUsersLoading = false
                invitationResultMessage = event.message
                invitationResultMessageColor = event.textColor
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (hasHousehold) {
            Button(
                onClick = { onEvent(ToHouseholdDetailsTapped) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.home_screen_my_household_label))
            }

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

@DarkAndLightPreviews
@Composable
fun ProfileDialogPreview(modifier: Modifier = Modifier) {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    val uiState =fakeRepo.getUserSignedInState(withHousehold = true)

    Preview(modifier.fillMaxWidth()) {
        ProfileDialog(
            modifier = Modifier,
            profileImageUrl = "",
            userName = "Test User",
            onDismiss = {},
            onLogout = {}
        ) {
            ProfileDialogContent(
                userInfo = uiState.user,
                hasHousehold = true,
                dialogDownstreamUiEvent = MutableSharedFlow(),
            ) { }
        }
    }
}
