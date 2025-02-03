package com.section11.expenselens.domain.usecase

import com.section11.expenselens.domain.models.ReceiptInformation
import com.section11.expenselens.domain.repository.ReceiptInfoExtractorRepository
import javax.inject.Inject

class ReceiptInformationUseCase @Inject constructor(
    private val receiptInfoExtractorRepository: ReceiptInfoExtractorRepository
) {

    suspend fun getReceiptInfo(extractedText: String): ReceiptInformation {
        return receiptInfoExtractorRepository.getReceiptInformation(extractedText)
    }
}
