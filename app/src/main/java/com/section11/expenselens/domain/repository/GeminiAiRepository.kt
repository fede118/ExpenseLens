package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.SuggestedExpenseInformation

interface ExpenseInfoExtractorRepository {

    suspend fun getExpenseInfo(text: String): Result<SuggestedExpenseInformation>
}
