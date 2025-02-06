package com.section11.expenselens.ui.camera

import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.domain.usecase.ImageToTextUseCase
import com.section11.expenselens.domain.usecase.ExpenseInformationUseCase
import com.section11.expenselens.framework.navigation.NavigationManager
import com.section11.expenselens.framework.navigation.NavigationManager.NavigationEvent.NavigateToExpensePreview
import com.section11.expenselens.ui.camera.CameraPreviewViewModel.CameraPreviewUiState.ShowCameraPreview
import com.section11.expenselens.ui.camera.event.CameraPreviewEvents
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
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
        // Set the main dispatcher to the test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize the ViewModel with the mocked dependencies
        viewModel = CameraPreviewViewModel(
            imageToTextUseCase,
            expenseInformationUseCase,
            navigationManager,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after the test
        Dispatchers.resetMain()
    }

    @Test
    fun `onImageCaptureTap should update uiState to Loading and then to ShowCameraPreview on success`() = runTest {
        // Given
        val extractedText = "Extracted Text"
        val expenseInformation: ExpenseInformation = mock()
        whenever(imageToTextUseCase.takePicture(any())).thenAnswer {
            val callback = it.getArgument<(Result<String>) -> Unit>(0)
            callback(Result.success(extractedText))
        }
        whenever(expenseInformationUseCase.getExpenseInfo(extractedText)).thenReturn(expenseInformation)

        // When
        viewModel.onUiEvent(CameraPreviewEvents.OnCaptureImageTapped)

        // Then
        val loadingState = viewModel.uiState.first()
        assertTrue(loadingState is UiState.Loading)
        advanceUntilIdle()
        val finalState = viewModel.uiState.first()
        assertTrue(finalState is ShowCameraPreview)

        // Verify that navigationManager.navigate is called with the correct arguments
        verify(navigationManager).navigate(
            NavigateToExpensePreview(extractedText, expenseInformation)
        )
    }

    @Test
    fun `onImageCaptureTap should update uiState to Error on failure`() = runTest {
        // Given
        val errorMessage = "Error extracting text"
        whenever(imageToTextUseCase.takePicture(any())).thenAnswer {
            val callback = it.getArgument<(Result<String>) -> Unit>(0)
            callback(Result.failure(Exception(errorMessage)))
        }

        // When
        viewModel.onUiEvent(CameraPreviewEvents.OnCaptureImageTapped)
        advanceUntilIdle()

        // Then
        val errorState = viewModel.uiState.first()
        assertTrue(errorState is UiState.Error)
        assertTrue((errorState as UiState.Error).message == errorMessage)
    }
}
