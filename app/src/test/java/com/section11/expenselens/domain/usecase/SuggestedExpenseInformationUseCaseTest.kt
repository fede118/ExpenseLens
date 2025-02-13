package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.repository.ExpenseInfoExtractorRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SuggestedExpenseInformationUseCaseTest {

    private val mockRepository: ExpenseInfoExtractorRepository = mock()

    private lateinit var useCase: ExpenseInformationUseCase

    @Before
    fun setup() {
        useCase = ExpenseInformationUseCase(mockRepository)
    }

    @Test
    fun `when extract from text is called then it should call repository`() = runTest {
        val expectedString = "anyString"

        useCase.getExpenseInfo(expectedString)

        verify(mockRepository).getExpenseInfo(expectedString)
    }
}
