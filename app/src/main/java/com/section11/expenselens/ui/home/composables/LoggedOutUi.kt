package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.BoxFullScreenContainer
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.SignInTapped
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview

@Composable
fun LoggedOutUi(modifier: Modifier = Modifier, onEvent: (HomeUpstreamEvent) -> Unit) {
    val dimens = LocalDimens.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    BoxFullScreenContainer(modifier) {
        Spacer(Modifier.height(dimens.m24))
        Image(
            painter = painterResource(id = R.drawable.playstore_icon),
            contentDescription = stringResource(R.string.content_description_app_icon),
            modifier = Modifier
                .size(dimens.m13)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(dimens.m6))
        Image(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.content_description_profile_pic),
            modifier = Modifier
                .size(dimens.m5)
                .clip(CircleShape)
                .clickable { onEvent(SignInTapped(context)) },
            colorFilter = ColorFilter.tint(colorScheme.contentColorFor(colorScheme.background))
        )
        Text(
            stringResource(R.string.home_screen_sign_in),
            Modifier.clickable { onEvent(SignInTapped(context)) }
        )
    }
}

@DarkAndLightPreviews
@Composable
fun LoggedOutScreenPreview() {
    Preview {
        LoggedOutUi {}
    }
}
