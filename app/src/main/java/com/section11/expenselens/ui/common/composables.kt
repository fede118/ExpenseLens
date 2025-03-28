package com.section11.expenselens.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.UiConstants.MAX_INPUT_CHARACTERS
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview

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
    maxLength: Int = MAX_INPUT_CHARACTERS,
    leadingIcon: @Composable () -> Unit = {}
) {
    val dimens = LocalDimens.current
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
        supportingText = { Text(text = "${value.length} / $maxLength") },
        shape = RoundedCornerShape(dimens.m4),
        leadingIcon = { leadingIcon() },
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
fun BoxedColumnFullScreenContainer(
    modifier: Modifier = Modifier,
    columnVerticalArrengement: Arrangement.Vertical = Arrangement.Top,
    columnHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    boxContent: @Composable BoxScope.() -> Unit = {},
    columnContent: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = columnVerticalArrengement,
            horizontalAlignment = columnHorizontalAlignment,
        ) {
            columnContent()
        }
        boxContent()
    }
}

@Composable
fun LabeledIcon(
    painterResource: Painter,
    label: String,
    modifier: Modifier = Modifier,
    backgroundSize: Dp = LocalDimens.current.m6,
    iconSize: Dp = LocalDimens.current.m3,
    contentDescription: String = label,
    colorFilter: ColorFilter? = ColorFilter.tint(colorScheme.contentColorFor(colorScheme.background)),
    onIconTap: () -> Unit
) {
    val dimens = LocalDimens.current

    Column(
        modifier
            .width(dimens.m10)
            .clip(RoundedCornerShape(dimens.m2))
            .clickable { onIconTap() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(backgroundSize)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                painter = painterResource,
                contentDescription = contentDescription,
                colorFilter = colorFilter
            )
        }
        if (label.isNotBlank()) {
            Spacer(Modifier.height(dimens.m1))
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BoxScope.AnimateFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val dimens = LocalDimens.current
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimens.m4)
            .align(Alignment.BottomCenter)
    ) {
        content()
    }
}

@DarkAndLightPreviews
@Composable
fun LabeledIconPreview() {
    Preview {
        Column(
            modifier = Modifier.statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LabeledIcon(
                painterResource = painterResource(R.drawable.user_icon),
                label = "Sign In"
            ) { }
        }
    }
}
