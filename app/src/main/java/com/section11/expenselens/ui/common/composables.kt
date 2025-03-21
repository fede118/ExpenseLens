package com.section11.expenselens.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.UiConstants.MAX_INPUT_CHARACTERS
import com.section11.expenselens.ui.home.model.UserInfoUiModel
import com.section11.expenselens.ui.theme.LocalDimens

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

@Composable
fun TransformingButton(
    buttonLabel: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    placeHolderText: String = String(),
    supportingText: @Composable () -> Unit = {},
    onSubmit: (String) -> Unit, // Callback with loading state control
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val dimens = LocalDimens.current
    var isEditing by remember { mutableStateOf(false) }


    AnimatedContent(
        targetState = isEditing to isLoading,
        transitionSpec = {
            (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
        },
        label = String()
    ) { (editing, loading) ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.size(dimens.m5))
                editing -> OutlinedTextFieldWithCallBack(
                    placeHolderText,
                    supportingText = { supportingText() },
                    onSubmit = { enteredText ->
                        onSubmit(enteredText)
                        keyboardController?.hide()
                    }
                )
                else -> {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.padding(dimens.m1)
                    ) {
                        Text(buttonLabel)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.OutlinedTextFieldWithCallBack(
    placeHolderText: String,
    modifier: Modifier = Modifier,
    supportingText: @Composable () -> Unit = {},
    onSubmit: (String) -> Unit
) {
    val dimens = LocalDimens.current
    var text by remember { mutableStateOf(String()) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier.weight(1f),
        placeholder = { Text(placeHolderText) },
        textStyle = MaterialTheme.typography.bodySmall,
        singleLine = true,
        supportingText = { supportingText() },
        trailingIcon = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text)
                        text = String()
                    }
                }
            ) {
                Text(stringResource(R.string.string_send))
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (text.isNotBlank()) {
                    onSubmit(text)
                    text = String()
                }
            }
        )
    )

    Spacer(modifier = Modifier.width(dimens.m1))
}

@Composable
fun ProfilePictureIcon(user: UserInfoUiModel, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(user.profilePic),
        contentDescription = stringResource(R.string.content_description_profile_pic),
        modifier = modifier
    )
}

@Composable
fun ContentWithBadge(
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        if (showBadge) {
            RedDot(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
fun RedDot(modifier: Modifier = Modifier) {
    val dimens = LocalDimens.current
    Box(
        modifier = modifier
            .padding(dimens.mHalf)
            .size(dimens.m1)
            .drawBehind {
                drawCircle(
                    color = Color.Red,
                    radius = size.minDimension / 1.5f,
                    center = Offset(size.width, size.height - dimens.m2.toPx())
                )
            }
    )
}

@Composable
fun BoxFullScreenContainer(
    modifier: Modifier = Modifier,
    boxContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = LocalDimens.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(dimens.m2)
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.m2)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
        boxContent()
    }
}
