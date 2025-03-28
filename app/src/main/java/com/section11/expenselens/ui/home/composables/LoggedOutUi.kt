package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.BoxedColumnFullScreenContainer
import com.section11.expenselens.ui.common.LabeledIcon
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview

@Composable
fun LoggedOutUi(modifier: Modifier = Modifier, onEvent: (HomeUpstreamEvent) -> Unit) {
    val dimens = LocalDimens.current
    val context = LocalContext.current

    BoxedColumnFullScreenContainer(
        modifier = modifier,
        columnVerticalArrengement = Arrangement.spacedBy(dimens.m6, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(R.drawable.playstore_icon),
            contentDescription = stringResource(R.string.content_description_app_icon),
            modifier = Modifier
                .size(dimens.m13)
                .clip(CircleShape)
        )
        LabeledIcon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painterResource = painterResource(R.drawable.user_icon),
            label = stringResource(R.string.home_screen_sign_in)
        ) {
            onEvent(SignInTapped(context))
        }
    }
}

@DarkAndLightPreviews
@Composable
fun LoggedOutScreenPreview() {
    Preview {
        LoggedOutUi {}
    }
}
