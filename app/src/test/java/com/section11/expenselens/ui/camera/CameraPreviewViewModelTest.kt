package com.section11.expenselens.ui.camera

import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.usecase.ExpenseInformationUseCase
import com.section11.expenselens.domain.usecase.ImageToTextUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents.OnCaptureImageTapped
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Error
import com.section11.expenselens.ui.utils.DownstreamUiEvent.Loading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CameraPreviewViewModelTest {

    private val imageToTextUseCase: ImageToTextUseCase = mock()
    private val expenseInformationUseCase: ExpenseInformationUseCase = mock()
    private val navigationManager: NavigationManager = mock()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: CameraPreviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        viewModel = CameraPreviewViewModel(
            imageToTextUseCase,
            expenseInformationUseCase,
            navigationManager,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onImageCaptureTap should update uiState to Loading and the remove the loader`() = runTest {
        // Given
        val extractedText = "Extracted Text"
        val suggestedExpenseInformation: SuggestedExpenseInformation = mock()
        whenever(imageToTextUseCase.takePicture(any())).thenAnswer {
            val callback = it.getArgument<(Result<String>) -> Unit>(0)
            callback(Result.success(extractedText))
        }
        whenever(expenseInformationUseCase.getExpenseInfo(extractedText))
            .thenReturn(Result.success(suggestedExpenseInformation))

        // When
        viewModel.onUiEvent(OnCaptureImageTapped)

        val event = viewModel.uiEvent.first()
        assert((event as? Loading)?.isLoading == true)
        advanceUntilIdle()

        // Verify that navigationManager.navigate is called with the correct arguments
        verify(navigationManager).navigate(NavigateToExpensePreview(extractedText, suggestedExpenseInformation))
    }

    @Test
    fun `onImageCaptureTap should update uiState to Error on failure`() = runTest {
        // Given
        val errorMessage = "Error extracting text"
        whenever(imageToTextUseCase.takePicture(any())).thenAnswer {
            val callback = it.getArgument<(Result<String>) -> Unit>(0)
            callback(Result.failure(Exception(errorMessage)))
        }

        // Since this is a cold flow we need to start the collection before actually calling the viewModel method
        val job = launch {
            viewModel.uiEvent.collect { result ->
                if (result is Error) {
                    assert(result.message == errorMessage)
                    cancel() // Cancel the coroutine after receiving the expected event
                }
            }
        }

        viewModel.onUiEvent(OnCaptureImageTapped)

        advanceUntilIdle()
        job.join() // Ensure the coroutine completes
    }
}
