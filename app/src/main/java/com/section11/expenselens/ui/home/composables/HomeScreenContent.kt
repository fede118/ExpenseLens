package com.section11.expenselens.ui.home.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.section11.expenselens.domain.models.ReceiptInformation
import com.section11.expenselens.ui.home.HomeViewModel
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.Idle
import com.section11.expenselens.ui.home.HomeViewModel.HomeUiState.TextExtractedFromImage
import com.section11.expenselens.ui.navigation.NavigationEvent
import com.section11.expenselens.ui.navigation.NavigationEvent.AddExpenseTapped
import com.section11.expenselens.ui.theme.ExpenseLensTheme
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: StateFlow<HomeViewModel.HomeUiState>,
    onNavigationEvent: (NavigationEvent) -> Unit = {}
) {
    val uiState by homeUiState.collectAsState()
    val dimens = LocalDimens.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.m2) // Add some padding to avoid screen edges
    ) {
        val (extractedTextFromImage, processedTextFromGemini) = when (uiState) {
            is Idle -> "No Expenses recorded" to null
            is TextExtractedFromImage -> with((uiState as TextExtractedFromImage)) {
                extractedText to processedTextByGemini
            }
        }

        Column(modifier = modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = dimens.m2)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
        ) {
            Greeting()

            Spacer(modifier = Modifier.padding(dimens.m2))

            Text(
                text = "Extracted Text from Image:\n $extractedTextFromImage",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.padding(dimens.m2))

            Text(
                text = "Processed Text from Gemini:\n $processedTextFromGemini",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (uiState == Idle) {
            FloatingActionButton(
                onClick = { onNavigationEvent(AddExpenseTapped) },
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
                homeUiState = MutableStateFlow(TextExtractedFromImage("Test", ReceiptInformation("$124,15", null))),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
