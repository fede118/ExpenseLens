package com.section11.expenselens.domain.repository

import com.section11.expenselens.domain.models.ReceiptInformation

interface ReceiptInfoExtractorRepository {

    suspend fun getReceiptInformation(text: String): ReceiptInformation
}
