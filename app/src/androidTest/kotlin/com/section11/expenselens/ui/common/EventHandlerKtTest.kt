package com.section11.expenselens.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EventHandlerKtTestTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenLoadingEventIsTrue_loaderIsDisplayed() = runTest {
        // Given
        val downstreamUiEvent = MutableSharedFlow<DownstreamUiEvent>()
        composeTestRule.setContent {
            HandleDownstreamEvents(
                downstreamUiEvent = downstreamUiEvent,
                modifier = Modifier.testTag("loader")
            )
        }

        // When
        downstreamUiEvent.emit(Loading(isLoading = true))
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithTag("loader").assertIsDisplayed()
    }

    @Test
    fun whenLoadingEventIsFalse_loaderIsNotDisplayed() = runTest {
        // Given
        val downstreamUiEvent = MutableSharedFlow<DownstreamUiEvent>()
        composeTestRule.setContent {
            HandleDownstreamEvents(
                downstreamUiEvent = downstreamUiEvent,
                modifier = Modifier.testTag("loader")
            )
        }

        // When
        downstreamUiEvent.emit(Loading(isLoading = false))
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithTag("loader").assertDoesNotExist()
    }

    @Test
    fun whenNoEventEmitted_loaderIsNotDisplayed() = runTest {
        // Given
        val downstreamUiEvent = MutableSharedFlow<DownstreamUiEvent>()
        composeTestRule.setContent {
            HandleDownstreamEvents(
                downstreamUiEvent = downstreamUiEvent,
                modifier = Modifier.testTag("loader")
            )
        }

        // When
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithTag("loader").assertIsNotDisplayed()
    }
}
