package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.SuggestedExpenseInformation
import com.section11.expenselens.domain.repository.ExpenseInfoExtractorRepository
import javax.inject.Inject

class ExpenseInformationUseCase @Inject constructor(
    private val expenseInfoExtractorRepository: ExpenseInfoExtractorRepository
) {

    suspend fun getExpenseInfo(extractedText: String): Result<SuggestedExpenseInformation> {
        return expenseInfoExtractorRepository.getExpenseInfo(extractedText)
    }
}
