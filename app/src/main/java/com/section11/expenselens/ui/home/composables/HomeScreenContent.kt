package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.home.HomeViewModel.HomeEvent.AddExpenseTapped
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: StateFlow<UiState>,
    onEvent: (HomeViewModel.HomeEvent) -> Unit
) {
    val uiState by homeUiState.collectAsState()
    val dimens = LocalDimens.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.m2) // Add some padding to avoid screen edges
    ) {

        Column(modifier = modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = dimens.m2)
            .statusBarsPadding()
        ) {
            Greeting()

            Spacer(modifier = Modifier.padding(dimens.m2))
        }

        if (uiState == UiState.Idle) {
            FloatingActionButton(
                onClick = { onEvent(AddExpenseTapped) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(dimens.m2)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Row {
        Text(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .statusBarsPadding(),
            text = "Hello, Welcome to ExpenseLens",
            textAlign = TextAlign.Center,
        )
    }

}

@DarkAndLightPreviews
@Composable
fun GreetingPreview() {
    ExpenseLensTheme {
        Surface {
            HomeScreenContent(
                homeUiState = MutableStateFlow(UiState.Idle),
                modifier = Modifier.fillMaxSize()
            ) {}
        }
    }
}
