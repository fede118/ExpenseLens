package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.repository.ReceiptInfoExtractorRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ReceiptInformationUseCaseTest {

    private val mockRepository: ReceiptInfoExtractorRepository = mock()

    private lateinit var useCase: ReceiptInformationUseCase

    @Before
    fun setup() {
        useCase = ReceiptInformationUseCase(mockRepository)
    }

    @Test
    fun `when extract from text is called then it should call repository`() = runTest {
        val expectedString = "anyString"

        useCase.getReceiptInfo(expectedString)

        verify(mockRepository).getReceiptInformation(expectedString)
    }
}
