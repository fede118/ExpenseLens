package com.section11.expenselens.ui.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddExpenseTapped
import com.section11.expenselens.ui.home.event.HomeUpstreamEvent.AddManualExpenseTapped
import com.section11.expenselens.ui.theme.LocalDimens

@Composable
fun BoxScope.AddExpenseButton(modifier: Modifier = Modifier, onEvent: (HomeUpstreamEvent) -> Unit) {
    val dimens = LocalDimens.current
    var expanded by remember { mutableStateOf(false) }

    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { expanded = false },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.m2))
            .background(MaterialTheme.colorScheme.primary)
            .align(Alignment.BottomEnd),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        AnimatedVisibility(
            expanded
        ) {
            BackHandler { expanded = false }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(dimens.m1)
            ) {
                Button(onClick = { onEvent(AddExpenseTapped) }) {
                    Text(stringResource(R.string.home_screen_add_expense_with_camera))
                }
                Button(onClick = { onEvent(AddManualExpenseTapped) }) {
                    Text(stringResource(R.string.home_screen_add_expense_manually))
                }
            }
        }

        AnimatedVisibility(!expanded) {
            FloatingActionButton(
                onClick = { expanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_screen_add_expense_label)
                )
            }
        }
    }
}
