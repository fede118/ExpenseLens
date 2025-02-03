package com.section11.expenselens.ui.home

import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ReceiptInformation
import com.section11.expenselens.domain.usecase.ReceiptInformationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockReceiptInformationUseCase: ReceiptInformationUseCase = mock()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(mockReceiptInformationUseCase, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when text extracted from image then should update Ui state`() = runTest {
        whenever(mockReceiptInformationUseCase.getReceiptInfo("anyString")).thenReturn(
            ReceiptInformation("total", Category.GROCERIES)
        )

        viewModel.onTextExtractedFromImage("anyString")
        advanceUntilIdle()

        assert(viewModel.uiState.value is HomeViewModel.HomeUiState.TextExtractedFromImage)
    }
}
