package com.section11.expenselens.data.repository

import com.section11.expenselens.data.dto.request.GeminiRequestBody
import com.section11.expenselens.data.dto.request.GeminiRequestBody.Content
import com.section11.expenselens.data.mapper.GeminiResponseMapper
import com.section11.expenselens.data.service.GeminiService
import com.section11.expenselens.domain.models.Category
import com.section11.expenselens.domain.models.ReceiptInformation
import com.section11.expenselens.domain.repository.ReceiptInfoExtractorRepository
import com.section11.expenselens.framework.networking.safeApiCall
import javax.inject.Inject

class GeminiAiRepository @Inject constructor(
    private val service: GeminiService,
    private val apiKey: String,
    private val geminiResponseMapper: GeminiResponseMapper
) : ReceiptInfoExtractorRepository {

    override suspend fun getReceiptInformation(text: String): ReceiptInformation {
        val requestBody = getGeminiRequestBody(RECEIPT_INFO_EXTRACTION_PROMPT + text)
        val response = safeApiCall {
            service.generateContent(requestBody, apiKey)
        }

        return geminiResponseMapper.toReceiptInformation(response)
    }

    private fun getGeminiRequestBody(text: String): GeminiRequestBody {
        return GeminiRequestBody(listOf(Content(listOf(Content.Part(text)))))
    }

    companion object {
        private var jsonStructure: String = ReceiptInformation.generateJsonStructure()
        private var categoriesToString = Category.getCategoryDescriptions()
        private var RECEIPT_INFO_EXTRACTION_PROMPT: String = """
            Following is text extracted from an image of a receipt. 
            I want you to reply ONLY with a json structured like this:
            ```
            $jsonStructure
            ```
            WHERE:
            - total: a representation of the total in that receipt 
            including gratuity/tips if it applies.
            - category: OPTIONAL, the best estimation of the Category of the receipt. Based on the following categories:
            $categoriesToString.
            IMPORTANT: the category must be in lowercase
            If it doesn't match any category leave it as null
            
            Here is the INPUT text from the receipt: \n"
            """
    }
}
