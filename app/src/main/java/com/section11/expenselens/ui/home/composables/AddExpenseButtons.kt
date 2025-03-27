package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.LabeledIcon
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddManualExpenseTapped
import com.section11.expenselens.ui.theme.LocalDimens

@Composable
fun AddExpenseButtons(modifier: Modifier = Modifier, onEvent: (HomeUpstreamEvent) -> Unit) {
    val dimens = LocalDimens.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        LabeledIcon(
            painterResource = painterResource(R.drawable.manual_entry_icon),
            label = stringResource(R.string.home_screen_add_expense_manually)
        ) { onEvent(AddManualExpenseTapped) }
        Spacer(Modifier.width(dimens.m5))
        LabeledIcon(
            painterResource = painterResource(R.drawable.camera_icon),
            label = stringResource(R.string.home_screen_add_expense_with_camera),
        ) { onEvent(AddExpenseTapped) }
    }
}
