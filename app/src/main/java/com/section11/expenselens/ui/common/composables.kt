package com.section11.expenselens.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.UiConstants.MAX_INPUT_CHARACTERS
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview

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
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
        title = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        },
        confirmButton = {} // No need for buttons, dismiss outside
    )
}

@Composable
fun ExpenseLensLoader(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseLensDatePicker(
    modifier: Modifier = Modifier,
    selectedDate: String = String(),
    onValueChange: (selectedDateMillis: Long) -> Unit = {}
) {
    val dimens = LocalDimens.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.m2),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showDatePicker = true }) {
            Text(text = stringResource(R.string.date_picker_title))
        }

        Spacer(modifier = Modifier.height(dimens.m2))

        Text(text = stringResource(R.string.date_picker_selected_label, selectedDate))

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { onValueChange(it) }
                        }
                    ) {
                        Text(stringResource(R.string.string_ok_caps))
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = true
                )
            }
        }
    }
}


@Composable
fun MaxCharsOutlinedTextField(
    value: String,
    title: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = MAX_INPUT_CHARACTERS
) {
    OutlinedTextField(
        value = value,
        modifier = modifier,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        singleLine = true,
        label = { Text(text = title) },
        supportingText = { Text(text = "${value.length} / $maxLength") }
    )
}

@DarkAndLightPreviews
@Composable
fun ProfileDialogPreview(modifier: Modifier = Modifier) {
    Preview(modifier.fillMaxWidth()) {
        ProfileDialog(
            modifier = Modifier,
            profileImageUrl = "",
            userName = "Test User",
            onDismiss = {},
            onLogout = {}
        )
    }
}

@Composable
fun ProfilePictureIcon(user: UserInfoUiModel, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(user.profilePic),
        contentDescription = stringResource(R.string.home_screen_sign_in_icon_content_description),
        modifier = modifier
    )
}
