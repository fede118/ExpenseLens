package com.section11.expenselens.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews

@Composable
fun ProfileDialog(
    modifier: Modifier = Modifier,
    profileImageUrl: String?,
    userName: String?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
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

        // Logout Button
        Button(onClick = { onLogout() }) {
            Text("Sign Out")
        }
    }
}


@Composable
fun CardDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    val dimens = LocalDimens.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.padding(dimens.m2),
        properties = DialogProperties(dismissOnClickOutside = true),
        title = {
            Column(
                modifier = Modifier.padding(dimens.m2).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        },
        confirmButton = {} // No need for buttons, dismiss outside
    )
}

@DarkAndLightPreviews
@Composable
fun ProfileDialogPreview(modifier: Modifier = Modifier) {
    ExpenseLensTheme {
        Surface(modifier.fillMaxWidth()) {
            ProfileDialog(
                modifier = Modifier,
                profileImageUrl = "",
                userName = "Test User",
                onDismiss = {},
                onLogout = {}
            )
        }
    }
}
