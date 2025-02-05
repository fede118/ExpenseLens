package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.ExpenseInformation

interface ExpenseInfoExtractorRepository {

    suspend fun getExpenseInfo(text: String): ExpenseInformation
}
